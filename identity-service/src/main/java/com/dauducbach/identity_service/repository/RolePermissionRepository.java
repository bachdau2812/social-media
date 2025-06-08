package com.dauducbach.identity_service.repository;

import com.dauducbach.identity_service.entity.RolePermission;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RolePermissionRepository extends ReactiveCrudRepository<RolePermission, String> {
    @Query("select permission_name from role_permissions where role_name like :roleName")
    Flux<String> findByRoleName(String roleName);

    @Query("SELECT COUNT(*) FROM role_permissions WHERE role_name = :roleName AND permission_name = :permissionName")
    Mono<Integer> countByRoleNameAndPermissionName(String roleName, String permissionName);

    @Modifying
    @Query("DELETE FROM role_permissions WHERE role_name = :roleName")
    Mono<Void> deleteByRoleName(String roleName);

    @Modifying
    @Query("DELETE FROM role_permissions WHERE role_name = :roleName AND permission_name = :permissionName")
    Mono<Void> deleteByRoleNameAndPermissionName(String roleName, String permissionName);
}
