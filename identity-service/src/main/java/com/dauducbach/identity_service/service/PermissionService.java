package com.dauducbach.identity_service.service;

import com.dauducbach.identity_service.dto.request.PermissionCreationRequest;
import com.dauducbach.identity_service.dto.response.PermissionResponse;
import com.dauducbach.identity_service.entity.Permission;
import com.dauducbach.identity_service.mapper.PermissionMapper;
import com.dauducbach.identity_service.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    R2dbcEntityTemplate r2dbcEntityTemplate;

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<PermissionResponse> createPermission(PermissionCreationRequest request) {
        log.info("Creating permission {}", request);
        return permissionRepository.existsById(request.getPermissionName()).flatMap(
                existed -> {
                    if(existed){
                        return Mono.error(new RuntimeException("Permission already existed"));
                    }
                    var permission = permissionMapper.toPermission(request);
                    return r2dbcEntityTemplate.insert(Permission.class).using(permission).map(permissionMapper::toPermissionResponse);
                }
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<PermissionResponse> getPermission(String permissionName) {
        return permissionRepository.findById(permissionName)
                .switchIfEmpty(Mono.error(new RuntimeException("Permission not exists")))
                .map(permissionMapper::toPermissionResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Flux<PermissionResponse> getAllPermission(){
        return permissionRepository.findAll().map(permissionMapper::toPermissionResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deletePermission(String permissionName) {
        return permissionRepository.deleteById(permissionName);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deleteAllPermission() {
        return permissionRepository.deleteAll();
    }
}
