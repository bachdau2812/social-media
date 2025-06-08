package com.dauducbach.profile_service.service;

import com.dauducbach.profile_service.dto.request.*;
import com.dauducbach.profile_service.entity.Profile;
import com.dauducbach.profile_service.repository.ProfileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProfileOperations {
    ProfileRepository profileRepository;

    public Mono<String> addFriends(AddFriendsRequest request) {
        return profileRepository.addFriend(request.getFromUserId(), request.getToUserId())
                .onErrorReturn("Can not invite friend");
    }

    public Mono<String> acceptFriends(AcceptFriendRequest request) {
        return profileRepository.acceptFriend(request.getFromUserId(), request.getToUserId())
                .onErrorResume(e -> Mono.just("Error accepting friend request: " + e.getMessage()))
                .defaultIfEmpty("No pending friend request found");
    }

    public Mono<String> unFriends(UnfriendRequest request) {
        return profileRepository.deleteFriend(request.getFromUserId(), request.getToUserId())
                .onErrorResume(e -> Mono.just("Error unFriend request: " + e.getMessage()))
                .defaultIfEmpty("No unfriend request found");
    }

    public Mono<String> block(BlockRequest request) {
        return profileRepository.blockUser(request.getFromUserId(), request.getToUserId())
                .onErrorResume(e -> Mono.just("Error block user request: " + e.getMessage()))
                .defaultIfEmpty("No block user request found");
    }

    public Mono<String> unBlock(UnBlockRequest request) {
        return profileRepository.unBlockUser(request.getFromUserId(), request.getToUserId())
                .onErrorResume(e -> Mono.just("Error unBlock user request: " + e.getMessage()))
                .defaultIfEmpty("No unBlock user request found");
    }

    public Mono<String> restriction (RestrictionRequest request) {
        return profileRepository.restrictionUser(request.getFromUserId(), request.getToUserId())
                .onErrorResume(e -> Mono.just("Error restriction user request: " + e.getMessage()))
                .defaultIfEmpty("No restriction user request found");
    }

    public Mono<String> unRestriction (UnRestrictionRequest request) {
        return profileRepository.unRestrictionUser(request.getFromUserId(), request.getToUserId())
                .onErrorResume(e -> Mono.just("Error unRestriction user request: " + e.getMessage()))
                .defaultIfEmpty("No unRestriction user request found");
    }

    public Mono<String> followUser(FollowUser request) {
        return profileRepository.followUser(request.getFromUserId(), request.getToUserId())
                .onErrorResume(e -> Mono.just("Error follow user request: " + e.getMessage()))
                .defaultIfEmpty("No follow user request found");
    }

    public Mono<String> unFollowUser(UnfollowUser request) {
        return profileRepository.unFollowUser(request.getFromUserId(), request.getToUserId())
                .onErrorResume(e -> Mono.just("Error unFollow user request: " + e.getMessage()))
                .defaultIfEmpty("No unFollow user request found");
    }

    public Flux<Profile> debugInvitations(Long userId) {
        log.info("Executing query for userId: {}", userId);
        return profileRepository.findAllInvitationsByUserId(userId)
                .doOnNext(p -> log.info("Found profile ID: {}", p.getId()))
                .doOnComplete(() -> log.info("Query completed"))
                .doOnError(e -> log.error("Error: ", e));
    }
}
