package com.dauducbach.identity_service.repository;

import com.dauducbach.identity_service.entity.UserRole;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, String> {
    @Query("select role_name from user_roles where user_id like :userId")
    Flux<String> findByUserId(Long userId);

    @Query("SELECT COUNT(*) FROM user_roles WHERE user_id = :userId AND role_name = :roleName")
    Mono<Integer> countByUserIdAndRoleName(Long userId, String roleName);

    @Modifying
    @Query("DELETE FROM user_roles WHERE user_id = :userId")
    Mono<Void> deleteByUserId(Long userId);
}
