package com.dauducbach.search_service.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTest {

    private final ReactiveStringRedisTemplate redisTemplate;

    @PostConstruct
    public void testRedisConnection() {
        String testKey = "redis:test:key";
        String testValue = "✅ Reactive Redis OK";

        redisTemplate.opsForValue()
                .set(testKey, testValue)
                .then(redisTemplate.opsForValue().get(testKey))
                .doOnNext(value -> log.info("🔁 Reactive Redis returned: {}", value))
                .doOnError(error -> log.error("❌ Redis connection failed", error))
                .subscribe(); // important: trigger execution
    }
}