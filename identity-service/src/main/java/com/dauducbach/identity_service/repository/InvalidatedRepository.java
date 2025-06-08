package com.dauducbach.identity_service.repository;

import com.dauducbach.identity_service.entity.InvalidatedToken;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface InvalidatedRepository extends ReactiveCrudRepository<InvalidatedToken, String> {
}
