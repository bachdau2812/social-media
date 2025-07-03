package com.dauducbach.api_gateway.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class RateLimiterService {
    ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<Boolean> isAllowedSlidingWindow (ServerWebExchange exchange, String endPoint) {
        return exchange.getPrincipal()
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("1a. No principal found!");
                    return Mono.empty();
                }))
                .map(Principal::getName)
                .flatMap(username -> {
                    String key = "rate_limit:swc:" + username + ":" + endPoint;

                    long now = Instant.now().toEpochMilli();
                    long windowSize = Duration.ofMinutes(1).toMillis();
                    long windowStart = now - windowSize;
                    int limit = 1000000;

                    Range<Double> range = Range.closed(0.0,(double) windowStart);

                    return redisTemplate.opsForZSet().removeRangeByScore(key, range)
                            .then(redisTemplate.opsForZSet().size(key))
                            .flatMap(count -> {
                                if (count < limit) {
                                    return redisTemplate.opsForZSet().add(key, UUID.randomUUID().toString(), now)
                                            .then(redisTemplate.expire(key, Duration.ofMinutes(2)))
                                            .thenReturn(true);
                                } else {
                                    return Mono.just(false);
                                }
                            })
                            .doOnError(e -> log.error("X. Rate limiter error", e));
                });
    }
}