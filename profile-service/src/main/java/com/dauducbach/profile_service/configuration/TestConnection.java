package com.dauducbach.profile_service.configuration;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.ReactiveNeo4jClient;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component

public class TestConnection {
    public ReactiveNeo4jClient neo4jClient;

    @PostConstruct
    public void testConnection() {
        neo4jClient.query("Return 1")
                .fetch()
                .one()
                .doOnSuccess(result -> log.info("✅ Kết nối thành công với Neo4j qua ReactiveNeo4jClient!"))
                .doOnError(error -> log.error("❌ Lỗi kết nối Neo4j: {}", error.getMessage()))
                .subscribe();
    }
}
