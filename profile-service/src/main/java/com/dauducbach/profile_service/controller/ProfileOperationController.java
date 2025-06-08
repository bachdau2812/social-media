package com.dauducbach.profile_service.controller;

import com.dauducbach.profile_service.dto.request.*;
import com.dauducbach.profile_service.entity.Profile;
import com.dauducbach.profile_service.service.ProfileOperations;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

@RequestMapping("/operations")
public class ProfileOperationController {
    ProfileOperations profileOperations;

    @PostMapping("/addfr")
    Mono<String> addFr(@RequestBody AddFriendsRequest request) {
        return profileOperations.addFriends(request);
    }

    @PostMapping("/acceptfr")
    Mono<String> acpFr(@RequestBody AcceptFriendRequest request) {
        return profileOperations.acceptFriends(request);
    }

    @PostMapping("/unfr")
    Mono<String> unFr(@RequestBody UnfriendRequest request) {
        return profileOperations.unFriends(request);
    }

    @PostMapping("/block")
    Mono<String> block(@RequestBody BlockRequest request) {
        return profileOperations.block(request) ;
    }

    @PostMapping("/unblock")
    Mono<String> unBlock(@RequestBody UnBlockRequest request) {
        return profileOperations.unBlock(request);
    }

    @PostMapping("/restriction")
    Mono<String> restriction(@RequestBody RestrictionRequest request) {
        return profileOperations.restriction(request);
    }

    @PostMapping("/unrestriction")
    Mono<String> unRestriction(@RequestBody UnRestrictionRequest request) {
        return profileOperations.unRestriction(request);
    }

    @PostMapping("/follow")
    Mono<String> follow(@RequestBody FollowUser request) {
        return profileOperations.followUser(request);
    }

    @PostMapping("/unfollow")
    Mono<String> unFollow(@RequestBody UnfollowUser request) {
        return profileOperations.unFollowUser(request);
    }

    @GetMapping("/friends-request/{userId}")
    Flux<Profile> getFriendsRequest (@PathVariable Long userId) {
        return profileOperations.debugInvitations(userId);
    }

}




















