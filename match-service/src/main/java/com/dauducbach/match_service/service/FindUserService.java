package com.dauducbach.match_service.service;

import co.elastic.clients.json.JsonData;
import com.dauducbach.match_service.dto.CurrentLocation;
import com.dauducbach.match_service.dto.request.FindUserAroundRequest;
import com.dauducbach.match_service.dto.response.ProfileResponse;
import com.dauducbach.match_service.entity.ProfileIndex;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FindUserService {
    ReactiveRedisTemplate<String, CurrentLocation> reactiveRedisTemplate;
    ReactiveElasticsearchOperations elasticsearchOperations;
    WebClient webClient;

    public Flux<ProfileResponse> findMatchUser(FindUserAroundRequest request) {
        return findUser(request)
                .collectList()
                .flatMapMany(listUserId -> getMatch()
                        .flatMapMany(Flux::fromIterable)
                        .filter(profileResponses -> listUserId.contains(String.valueOf(profileResponses.getUserId())))
                );
    }

    public Flux<String> findUser(FindUserAroundRequest request) {
        log.info("Request: {}", request);
        CurrentLocation currentLocation = CurrentLocation.builder()
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
        log.info("Current location: {}", currentLocation);

        return reactiveRedisTemplate.keys("location:user:*")
                .flatMap(key -> reactiveRedisTemplate.opsForValue().get(key)
                        .map(value -> Map.entry(key, value))
                )
                .doOnNext(stringCurrentLocationEntry -> log.info("Dest Location: {}", stringCurrentLocationEntry.getValue()))
                .filter(entry -> computeDistance(currentLocation, entry.getValue()) <= request.getRadiusKm())
                .map(entry -> entry.getKey().replace("location:user:", ""));
    }

    public Mono<List<ProfileResponse>> getMatch() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(Jwt.class)
                .map(Jwt::getTokenValue)
                .flatMap(token -> webClient.get()
                        .uri("http://localhost:8081/profile/my-info")
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(ProfileResponse.class)
                        .map(ProfileResponse::getId)
                        .flatMap(profileId -> elasticsearchOperations.get(String.valueOf(profileId), ProfileIndex.class))
                        .flatMapMany(profileIndex -> {
                            NativeQuery nativeQuery = NativeQuery.builder()
                                    .withQuery(q -> q
                                            .scriptScore(ss -> ss
                                                    .query(innerQuery -> innerQuery
                                                            .matchAll(m -> m)
                                                    )
                                                    .script(script -> script
                                                            .source("cosineSimilarity(params.query_vector, 'embeddings') + 1.0")
                                                            .params(Map.of("query_vector", JsonData.of(profileIndex.getEmbeddings()))
                                                            )
                                                    )
                                            )
                                    )
                                    .withPageable(Pageable.ofSize(5))
                                    .build();

                            return elasticsearchOperations.search(nativeQuery, ProfileIndex.class)
                                    .map(SearchHit::getContent)
                                    .filter(profileIndex1 -> !Objects.equals(profileIndex1.getId(), profileIndex.getId()))
                                    .flatMap(profileIndex2 -> webClient.get()
                                            .uri("http://localhost:8081/profile/{profileId}", profileIndex2.getId())
                                            .header("Authorization", "Bearer " + token)
                                            .retrieve()
                                            .bodyToMono(ProfileResponse.class)
                                    );
                        }).collectList()
                );
    }

    private double computeDistance (CurrentLocation location1, CurrentLocation location2) {
        final int R = 6371; // Bán kính Trái Đất (km)

        double dLat = Math.toRadians(location2.getLatitude() - location1.getLatitude());
        double dLon = Math.toRadians(location2.getLongitude() - location1.getLongitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(location1.getLatitude())) * Math.cos(Math.toRadians(location2.getLatitude()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        log.info("Distance: {}", R*c);
        return R * c; // Khoảng cách tính bằng km
    }
}