package com.dauducbach.chat_service.controller;

import com.dauducbach.chat_service.dto.request.PingRequest;
import com.dauducbach.chat_service.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    PresenceService presenceService;

    @MessageMapping("/ping")
    public Mono<Void> handlePing(@Payload PingRequest pingRequest) {
        return presenceService.refreshLastSeen(pingRequest.getUserId());
    }
}
