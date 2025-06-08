package com.dauducbach.post_service.repository;

import com.dauducbach.post_service.entity.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PostRepository extends ReactiveMongoRepository<Post, String> {
    Flux<Post> findAllByUserId(String userId);
    Mono<Void> deleteAllByUserId(String userId);
}