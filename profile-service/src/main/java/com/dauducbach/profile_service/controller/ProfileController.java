package com.dauducbach.profile_service.controller;

import com.dauducbach.profile_service.dto.request.ProfileCreationRequest;
import com.dauducbach.profile_service.dto.request.ProfileUpdateRequest;
import com.dauducbach.profile_service.dto.response.ProfileResponse;
import com.dauducbach.profile_service.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j

public class ProfileController {
    ProfileService profileService;

    @PostMapping("/registration")
    Mono<ProfileResponse> create(@RequestBody ProfileCreationRequest request) {
        return profileService.create(request);
    }

    @GetMapping("/my-info")
    Mono<ProfileResponse> getMyInfo() {
        return profileService.getMyInfo();
    }

    @PutMapping("/{profileId}")
    Mono<ProfileResponse> update(@PathVariable long profileId, @RequestBody ProfileUpdateRequest request) {
        return profileService.update(profileId, request);
    }

    @GetMapping("/{profileId}")
    Mono<ProfileResponse> getByProfileId(@PathVariable long profileId) {
        return profileService.getByProfileId(profileId);
    }

    @GetMapping("/get-by-user-id/{userId}")
    Mono<ProfileResponse> getByUserId(@PathVariable long userId) {
        return profileService.getProfileByUserId(userId);
    }

    @GetMapping("/get-all")
    Flux<ProfileResponse> getAll(){
        return profileService.getAllProfile();
    }

    @DeleteMapping("/{profileId}")
    Mono<Void> deleteByProfileId(@PathVariable long profileId){
        return profileService.deleteProfileByProfileId(profileId);
    }

    @DeleteMapping("/delete-by-user-id/{userId}")
    Mono<Void> deleteByUserId(@PathVariable long userId) {
        return profileService.deleteProfileByUserId(userId);
    }

    @DeleteMapping("/delete-all")
    Mono<Void> deleteAll(){
        return profileService.deleteAll();
    }
}
