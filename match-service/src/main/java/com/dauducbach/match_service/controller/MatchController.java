package com.dauducbach.match_service.controller;

import com.dauducbach.match_service.dto.response.ProfileResponse;
import com.dauducbach.match_service.service.FindUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class MatchController {
    FindUserService findUserService;

    @GetMapping("/list-map")
    Mono<List<ProfileResponse>> getMatch() {
        return findUserService.getMatch();
    }
}
