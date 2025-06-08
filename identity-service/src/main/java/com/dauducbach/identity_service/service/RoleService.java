package com.dauducbach.identity_service.service;

import com.dauducbach.identity_service.dto.request.AddPermissionForRoleRequest;
import com.dauducbach.identity_service.dto.request.DeletePermissionOfRoleRequest;
import com.dauducbach.identity_service.dto.request.RoleCreationRequest;
import com.dauducbach.identity_service.dto.request.RoleUpdateRequest;
import com.dauducbach.identity_service.dto.response.RoleResponse;
import com.dauducbach.identity_service.entity.Role;
import com.dauducbach.identity_service.entity.RolePermission;
import com.dauducbach.identity_service.mapper.RoleMapper;
import com.dauducbach.identity_service.repository.PermissionRepository;
import com.dauducbach.identity_service.repository.RolePermissionRepository;
import com.dauducbach.identity_service.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleService {
    RoleRepository roleRepository;
    RoleMapper roleMapper;
    PermissionRepository permissionRepository;
    RolePermissionRepository rolePermissionRepository;

    R2dbcEntityTemplate r2dbcEntityTemplate;


    @PreAuthorize("hasRole('ADMIN')")
    public Mono<RoleResponse> createRole(RoleCreationRequest request) {
        RoleResponse roleResponse = new RoleResponse();
        return Mono.zip(
                isValid(request.getPermissions()),
                roleRepository.existsById(request.getRoleName())
        ).flatMap(
                tupple -> {
                    boolean isValidPermission = tupple.getT1();
                    boolean isExisted = tupple.getT2();

                    if(isExisted) {
                        return Mono.error(new RuntimeException("Role already existed"));
                    }

                    if(!isValidPermission) {
                        return Mono.error(new RuntimeException("Invalid Permission"));
                    }

                    var role = roleMapper.toRole(request);

                    return r2dbcEntityTemplate.insert(Role.class).using(role).flatMap(
                            savedRole -> Flux.fromIterable(request.getPermissions()).flatMap(
                                    permission -> rolePermissionRepository.countByRoleNameAndPermissionName(savedRole.getRoleName(), permission)
                                            .map(count -> count > 0)
                                            .flatMap(
                                                    existed -> {
                                                        if(existed) {
                                                            log.info("Permission {} existed for Role {}", permission, savedRole.getRoleName());
                                                            return Mono.just(true);
                                                        }else{
                                                            RolePermission newEntry = new RolePermission(savedRole.getRoleName(), permission);
                                                            return r2dbcEntityTemplate.insert(RolePermission.class).using(newEntry);
                                                        }
                                                    }
                                            )
                            ).then(Mono.just(roleMapper.toRoleResponse(savedRole)))
                                    .map(roleResponse2 -> {
                                        roleResponse2.setPermissions(request.getPermissions());
                                        return roleResponse2;
                                    })
                    );
                }
        );
    }
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<RoleResponse> getAllRole(){
        return roleRepository.findAll().map(roleMapper::toRoleResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<RoleResponse> updateRole(RoleUpdateRequest request) {
        return Mono.zip(
                        roleRepository.findById(request.getRoleName()),
                        isValid(request.getPermissions())
                )
                .flatMap(
                        tuple -> {
                            boolean isValidPermission = tuple.getT2();
                            Role existed = tuple.getT1();
                            if(!isValidPermission) {
                                return Mono.error(new RuntimeException("Invalid Permission"));
                            }
                            if(existed == null) {
                                return Mono.error(new RuntimeException("Role not exists"));
                            }
                            return Flux.fromIterable(request.getPermissions())
                                    .flatMap(
                                            permission -> rolePermissionRepository.countByRoleNameAndPermissionName(request.getRoleName(), permission)
                                                    .map(count -> count > 0)
                                                    .flatMap(
                                                            existedPermission -> {
                                                                if(existedPermission) {
                                                                    log.info("Permission {} existed for Role {}", permission, request.getRoleName());
                                                                    return Mono.empty();
                                                                }else{
                                                                    RolePermission newEntry = new RolePermission(request.getRoleName(), permission);
                                                                    return r2dbcEntityTemplate.insert(RolePermission.class).using(newEntry);
                                                                }
                                                            }
                                                    )
                                    )
                                    .collectList()
                                    .map(rolePermissions -> rolePermissions.stream()
                                            .map(RolePermission::getPermissionName)
                                            .collect(Collectors.toSet()))
                                    .map(rolePermissions -> {
                                        RoleResponse roleResponse1 = roleMapper.toRoleResponse(existed);
                                        roleResponse1.setPermissions(rolePermissions);
                                        return roleResponse1;
                                    });
                        }
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> addPermission(AddPermissionForRoleRequest request, String roleName) {
        Flux.fromIterable(request.getAdd_permissions()).flatMap(
                permission -> rolePermissionRepository.countByRoleNameAndPermissionName(roleName, permission)
                        .map(count -> count > 0)
                        .flatMap(
                                existed -> {
                                    if(existed) {
                                        return Mono.just(true);
                                    }else{
                                        RolePermission newEntry = new RolePermission(roleName, permission);
                                        return r2dbcEntityTemplate.insert(RolePermission.class).using(newEntry);
                                    }
                                }
                        )
        ).subscribe();
        return Mono.empty();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deletePermission(DeletePermissionOfRoleRequest request, String roleName) {
        Flux.fromIterable(request.getDelete_permissions()).flatMap(
                permission -> rolePermissionRepository.deleteByRoleNameAndPermissionName(roleName, permission)
        ).subscribe();
        return Mono.empty();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deleteRole(String roleName) {
        rolePermissionRepository.deleteByRoleName(roleName).subscribe();
        return roleRepository.deleteById(roleName);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deleteAllRole() {
        rolePermissionRepository.deleteAll().subscribe();
        return roleRepository.deleteAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Boolean> isValid(Set<String> permissions) {
        return Flux.fromIterable(permissions).flatMap(
                permissionRepository::existsById
        ).all(Boolean::booleanValue);
    }


}
