package com.dauducbach.profile_service.configuration;

import com.dauducbach.profile_service.entity.ProfileIndex;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ReactiveIndexOperations;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Configuration
public class IndexInitializer {
    ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    @EventListener(ApplicationReadyEvent.class)
    public void createIndex(){
        ReactiveIndexOperations indexOps = reactiveElasticsearchOperations.indexOps(ProfileIndex.class);

        indexOps.exists()
                .flatMap(exists -> {
                    if (!exists) {
                        return indexOps.create()
                                .then(indexOps.putMapping())
                                .doOnSuccess(v -> System.out.println("Created new index with mapping"));
                    }
                    System.out.println("Index already exists - skipping creation");
                    return Mono.just(true);
                })
                .subscribe(
                        success -> System.out.println("Index initialization completed"),
                        error -> System.err.println("Index initialization failed: " + error)
                );
    }
}