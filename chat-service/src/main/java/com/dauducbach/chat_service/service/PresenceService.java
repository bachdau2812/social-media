package com.dauducbach.chat_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j

public class PresenceService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public Mono<Void> setOnline(String userId) {
        return reactiveRedisTemplate.opsForValue()
                .set("presence:" + userId, "online")
                .then(reactiveRedisTemplate.opsForValue()
                        .set("last_seen:" + userId, Instant.now().toString()))
                .then();
    }

    public Mono<Void> refreshLastSeen(String userId) {
        return reactiveRedisTemplate.opsForValue()
                .set("last_seen:" + userId, Instant.now().toString())
                .then();
    }

    public Mono<Void> setOffline(String userId) {
        return reactiveRedisTemplate.opsForValue()
                .delete("presence:" + userId)
                .then();
    }

    public Mono<Instant> getLastSeen(String userId) {
        return reactiveRedisTemplate.opsForValue()
                .get("last_seen:" + userId)
                .map(Instant::parse);
    }

    public Flux<String> getOnlineUserId() {
        return reactiveRedisTemplate.keys("presence:*")
                .flatMap(key -> reactiveRedisTemplate.opsForValue()
                        .get(key)
                        .filter(value -> value.equals("online"))
                        .map(value -> key)
                )
                .map(key -> key.replace("presence:", ""));
    }
}
