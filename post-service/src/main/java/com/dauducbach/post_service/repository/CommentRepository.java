package com.dauducbach.post_service.repository;

import com.dauducbach.post_service.entity.Comment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {
    Mono<Void> deleteByPostId(String postId);

}