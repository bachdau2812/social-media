package com.dauducbach.chat_service.controller;

import com.dauducbach.chat_service.dto.request.PingRequest;
import com.dauducbach.chat_service.service.PresenceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

@RequestMapping("/presence")
public class PresenceController {
    PresenceService presenceService;

    @GetMapping
    public Flux<String> getOnlineUser() {
        return presenceService.getOnlineUserId();
    }

    @PostMapping
    public Mono<Void> setOnl(@RequestBody PingRequest pingRequest) {
        return presenceService.setOnline(pingRequest.getUserId());
    }
}
