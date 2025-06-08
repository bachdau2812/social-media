package com.dauducbach.post_service.configuration;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoConnectionTest {

    public MongoConnectionTest(ReactiveMongoTemplate reactiveMongoTemplate) {
        reactiveMongoTemplate.getCollectionNames()
                .collectList()
                .doOnSuccess(collections -> System.out.println("✅ Kết nối MongoDB thành công! Collections: " + collections))
                .doOnError(error -> System.err.println("❌ Kết nối MongoDB thất bại: " + error.getMessage()))
                .subscribe();
    }
}