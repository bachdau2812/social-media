package com.dauducbach.match_service.controller;

import com.dauducbach.match_service.dto.request.FindUserAroundRequest;
import com.dauducbach.match_service.dto.request.LocationRequest;
import com.dauducbach.match_service.dto.response.ApiResponse;
import com.dauducbach.match_service.dto.response.ProfileResponse;
import com.dauducbach.match_service.entity.LocationHistory;
import com.dauducbach.match_service.service.FindUserService;
import com.dauducbach.match_service.service.LocationService;
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

public class LocationController {
    FindUserService findUserService;
    LocationService locationService;

    @GetMapping("/match")
    Flux<ProfileResponse> match(@RequestBody FindUserAroundRequest request) {
        return findUserService.findMatchUser(request);
    }

    @PostMapping("/save-location")
    Mono<LocationHistory> saveLocation (@RequestBody LocationRequest request) {
        return locationService.saveLocation(request);
    }

    @PostMapping("/update-location")
    Mono<LocationHistory> updateLocation (@RequestBody LocationRequest request) {
        return locationService.updateLocation(request);
    }

    @DeleteMapping("/delete-location/{userId}")
    Mono<String> deleteLocation (@PathVariable String userId) {
        return locationService.removeLocationInRedis(userId);
    }

    @GetMapping("/last-active/{userId}")
    Mono<LocationHistory> lastActive (@PathVariable String userId) {
        return locationService.lastActive(userId);
    }

    @PostMapping("/find-user")
    Flux<ApiResponse<String>> findUser(@RequestBody FindUserAroundRequest request) {
        return findUserService.findUser(request)
                .map(s -> ApiResponse.<String>builder()
                        .result(s)
                        .build());
    }
}