package com.dauducbach.profile_service.service;

import com.dauducbach.profile_service.dto.request.EmbeddingRequest;
import com.dauducbach.profile_service.dto.request.ProfileCreationRequest;
import com.dauducbach.profile_service.dto.request.ProfileUpdateRequest;
import com.dauducbach.profile_service.dto.response.EmbeddingResponse;
import com.dauducbach.profile_service.dto.response.ProfileResponse;
import com.dauducbach.profile_service.entity.Profile;
import com.dauducbach.profile_service.entity.ProfileIndex;
import com.dauducbach.profile_service.mapper.ProfileMapper;
import com.dauducbach.profile_service.repository.ProfileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class ProfileService {
    IdGenerator idGenerator;
    ProfileMapper profileMapper;
    ProfileRepository profileRepository;
    ReactiveElasticsearchOperations elasticsearchOperations;
    WebClient webClient;

    public Mono<ProfileResponse> create(ProfileCreationRequest request) {
        long id = idGenerator.nextId();
        log.info("1. Profile creation request: {}", request);
        Profile profile = profileMapper.toProfile(request);
        log.info("1. Profile: {}", profile);
        profile.setId(id);
        profile.setFriends(new ArrayList<>());
        profile.setFollowUser(new ArrayList<>());
        profile.setRestrictions(new ArrayList<>());
        profile.setBlocks(new ArrayList<>());
        profile.setInterests(request.getInterests());
        profile.setInvites(new ArrayList<>());
        profile.setFollowPage(request.getFollowPage());

        var profileIndex = profileMapper.toProfileIndex(profile);
        profileIndex.setId(String.valueOf(profile.getId()));
        profileIndex.setUsername(String.valueOf(profile.getUsername()));
        profileIndex.setInterests(profile.getInterests());
        log.info("1. Profile index: {}", profileIndex);

        return profileRepository.save(profile)
                .then(getEmbeddings(profileMapper.toProfileResponse(profile))
                        .flatMap(embeddings -> {
                            profileIndex.setEmbeddings(embeddings);
                            return elasticsearchOperations.save(profileIndex)
                                    .doOnError(ex -> log.info("Error while save profile index to elasticsearch: {}", ex.getMessage()));
                        }))
                .thenReturn(profileMapper.toProfileResponse(profile));
    }

    private String prepareEmbeddingInput(ProfileResponse profileResponse) {
        if (profileResponse == null) {
            log.info("Profile response is null");
            return null;
        }
        return String.join("\n",
                "Username: " + profileResponse.getUsername(),
                "City: ", profileResponse.getCity(),
                "Job: ", profileResponse.getJob(),
                "Vehicle: ", profileResponse.getVehicle(),
                "Interests: ", String.join(", ", profileResponse.getInterests()),
                "FollowPage: ", String.join(", ", profileResponse.getFollowPage()),
                "FollowUser: ", String.join(", ", profileResponse.getFollowUser())
        );
    }

    private Mono<List<Float>> getEmbeddings(ProfileResponse profileResponse) {
        return webClient.post()
                .uri("http://localhost:5000/embed")
                .bodyValue(EmbeddingRequest.builder()
                        .text(prepareEmbeddingInput(profileResponse))
                        .build())
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .doOnError(ex -> log.info("Error while get embedding: {}", ex.getMessage()))
                .map(EmbeddingResponse::getEmbedding);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ProfileResponse> getByProfileId(long id){
        return profileRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Profile not exists")))
                .doOnSuccess(profile -> elasticsearchOperations.save(profileMapper.toProfileIndex(profile)))
                .map(profileMapper::toProfileResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ProfileResponse> getProfileByUserId(long userId) {
        return profileRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("Profile of user not exists")))
                .map(profileMapper::toProfileResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Flux<ProfileResponse> getAllProfile(){
        return profileRepository.findAll()
                .map(profileMapper::toProfileResponse);
    }

    public Mono<ProfileResponse> update(long profileId, ProfileUpdateRequest request) {
        return profileRepository.findById(profileId)
                .switchIfEmpty(Mono.error(new RuntimeException("Profile not exists")))
                .map(profile -> profileMapper.updateProfile(request, profile))
                .flatMap(profile -> {
                    var profileIndex = profileMapper.toProfileIndex(profile);
                    return getEmbeddings(profileMapper.toProfileResponse(profile))
                            .flatMap(embeddings -> {
                                profileIndex.setEmbeddings(embeddings);
                                return elasticsearchOperations.save(profileIndex)
                                        .then(profileRepository.save(profile));
                            });
                })
                .map(profileMapper::toProfileResponse);
    }

    public Mono<Void> deleteProfileByProfileId(long profileId){
        return profileRepository.findById(profileId)
                .flatMap(profile -> elasticsearchOperations.delete(String.valueOf(profile.getId()), ProfileIndex.class))
                .then();
    }

    public Mono<Void> deleteProfileByUserId(long userId) {
        return profileRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("Profile not exists")))
                .flatMap(profile -> elasticsearchOperations.delete(String.valueOf(profile.getId()), ProfileIndex.class))
                .then(profileRepository.deleteByUserId(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deleteAll(){
        return profileRepository.findAll()
                .flatMap(profile -> elasticsearchOperations.delete(String.valueOf(profile.getId()), ProfileIndex.class))
                .then(profileRepository.deleteAll());
    }

    public Mono<ProfileResponse> getMyInfo(){
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(userId -> profileRepository.findByUserId(Long.parseLong(userId))
                        .switchIfEmpty(Mono.error(new RuntimeException("Profile not found")))
                        .map(profileMapper::toProfileResponse)
                );
    }
}
