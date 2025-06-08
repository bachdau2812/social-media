package com.dauducbach.identity_service.controller;

import com.dauducbach.identity_service.dto.request.PermissionCreationRequest;
import com.dauducbach.identity_service.dto.response.ApiResponse;
import com.dauducbach.identity_service.dto.response.PermissionResponse;
import com.dauducbach.identity_service.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

@RestController
@RequestMapping("/permissions")
public class PermissionController {
    PermissionService permissionService;

    @PostMapping
    Mono<ApiResponse<PermissionResponse>> createPermission(@RequestBody PermissionCreationRequest request) {
        log.info("Create permission: {}", request);
        return permissionService.createPermission(request).map(
                permissionResponse -> ApiResponse.<PermissionResponse>builder()
                        .result(permissionResponse)
                        .build()
        );
    }

    @GetMapping("/permission_name")
    Mono<ApiResponse<PermissionResponse>> getPermission(@PathVariable String permission_name) {
        return permissionService.getPermission(permission_name).map(
                permissionResponse -> ApiResponse.<PermissionResponse>builder()
                        .result(permissionResponse)
                        .build()
        );
    }

    @GetMapping
    Flux<ApiResponse<PermissionResponse>> getAllPermission() {
        return permissionService.getAllPermission().map(
                permissionResponse -> ApiResponse.<PermissionResponse>builder()
                        .result(permissionResponse)
                        .build()
        );
    }

    @DeleteMapping("/permission_name")
    Mono<ApiResponse<Void>> deletePermission(@PathVariable String permission_name) {
        return permissionService.deletePermission(permission_name).map(
                unused -> ApiResponse.<Void>builder().build()
        );
    }

    @DeleteMapping("/delete-all")
    Mono<ApiResponse<Void>> deletePermission() {
        return permissionService.deleteAllPermission().map(
                unused -> ApiResponse.<Void>builder().build()
        );
    }
}