package com.dauducbach.post_service.repository;

import com.dauducbach.post_service.entity.PostAction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ActionRepository extends ReactiveMongoRepository<PostAction, String> {
    Flux<PostAction> findAllByPostId(String postId);
    Mono<Void> deleteAllByPostId(String postId);
}