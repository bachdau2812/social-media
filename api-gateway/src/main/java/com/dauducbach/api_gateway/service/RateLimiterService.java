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
                .doOnSubscribe(sub -> log.info("1. Starting rate limit check - Subscription"))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("1a. No principal found!");
                    return Mono.empty();
                }))
                .map(Principal::getName)
                .doOnNext(username -> log.info("2. Processing user: {}", username))
                .flatMap(username -> {
                    String key = "rate_limit:swc:" + username + ":" + endPoint;
                    log.info("3. Using Redis key: {}", key);

                    long now = Instant.now().toEpochMilli();
                    long windowSize = Duration.ofMinutes(1).toMillis();
                    long windowStart = now - windowSize;
                    int limit = 10;


                    Range<Double> range = Range.closed(0.0,(double) windowStart);

                    return redisTemplate.opsForZSet().removeRangeByScore(key, range)
                            .doOnSuccess(v -> log.info("4. Removed old entries from ZSET"))
                            .then(redisTemplate.opsForZSet().size(key))
                            .doOnNext(size -> log.info("5. Current ZSET size: {}", size))
                            .flatMap(count -> {
                                if (count < limit) {
                                    return redisTemplate.opsForZSet().add(key, UUID.randomUUID().toString(), now)
                                            .doOnSuccess(b -> log.info("6. Added new entry to ZSET"))
                                            .then(redisTemplate.expire(key, Duration.ofMinutes(2)))
                                            .doOnSuccess(v -> log.info("7. Set expiration for key"))
                                            .thenReturn(true);
                                } else {
                                    log.warn("8. Rate limit exceeded for {}", key);
                                    return Mono.just(false);
                                }
                            })
                            .doOnError(e -> log.error("X. Rate limiter error", e))
                            .doOnSuccess(result -> log.info("9. Rate limit result: {}", result));
                });
    }
}