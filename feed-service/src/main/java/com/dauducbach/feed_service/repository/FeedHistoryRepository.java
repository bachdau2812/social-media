package com.dauducbach.feed_service.repository;

import com.dauducbach.feed_service.entity.FeedHistory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedHistoryRepository extends ReactiveMongoRepository<FeedHistory, String> {

}
