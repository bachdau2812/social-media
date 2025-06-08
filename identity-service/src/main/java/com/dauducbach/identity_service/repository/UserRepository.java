package com.dauducbach.identity_service.repository;

import com.dauducbach.identity_service.entity.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<Boolean> existsByUsername(String username);
    Mono<User> findByUsername(String username);
}
