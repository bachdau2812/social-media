package com.dauducbach.match_service.repository;

import com.dauducbach.match_service.entity.LocationHistory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface LocationHistoryRepository extends ReactiveMongoRepository<LocationHistory, String> {
    Flux<LocationHistory> findByUserId(String userId);
}