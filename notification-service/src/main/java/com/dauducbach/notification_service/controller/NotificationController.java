package com.dauducbach.notification_service.controller;

import com.dauducbach.event.NotificationEvent;
import com.dauducbach.notification_service.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class NotificationController {
    NotificationService notificationService;

    @PostMapping
    public Mono<String> sendNotification(@RequestBody NotificationEvent notificationEvent) {
        return notificationService.sendNotification(notificationEvent);
    }
}
