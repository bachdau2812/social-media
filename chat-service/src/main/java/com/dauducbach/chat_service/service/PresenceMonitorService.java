package com.dauducbach.chat_service.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class PresenceMonitorService implements ApplicationListener<ApplicationReadyEvent> {
    PresenceService presenceService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Flux.interval(Duration.ofSeconds(10))
                .flatMap(tick -> presenceService.getOnlineUserId())
                .flatMap(userId ->
                        presenceService.getLastSeen(userId)
                                .filter(lastSeen -> Duration.between(lastSeen, Instant.now()).compareTo(Duration.ofSeconds(30)) > 0)
                                .flatMap(expired -> presenceService.setOffline(userId))
                )
                .subscribe();
    }
}
