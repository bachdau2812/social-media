package com.dauducbach.identity_service.controller;

import com.dauducbach.identity_service.dto.request.RoleCreationRequest;
import com.dauducbach.identity_service.dto.request.RoleUpdateRequest;
import com.dauducbach.identity_service.dto.response.ApiResponse;
import com.dauducbach.identity_service.dto.response.RoleResponse;
import com.dauducbach.identity_service.service.RoleService;
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
@RequestMapping("/roles")
public class RoleController {
    RoleService roleService;

    @PostMapping
    Mono<ApiResponse<RoleResponse>> createRole(@RequestBody RoleCreationRequest request) {
        return roleService.createRole(request).map(
                roleResponse -> ApiResponse.<RoleResponse>builder()
                        .result(roleResponse)
                        .build()
        );
    }

    @GetMapping
    Flux<ApiResponse<RoleResponse>> getAllRole(){
        return roleService.getAllRole().map(
                roleResponse ->  ApiResponse.<RoleResponse>builder()
                        .result(roleResponse)
                        .build()
        );
    }

    @PutMapping
    Mono<ApiResponse<RoleResponse>> updateRole(@RequestBody RoleUpdateRequest request) {
        return roleService.updateRole(request).map(
                roleResponse ->  ApiResponse.<RoleResponse>builder()
                        .result(roleResponse)
                        .build()
        );
    }

    @DeleteMapping("/{role_name}")
    Mono<ApiResponse<Void>> deleteRole(@PathVariable String role_name) {
        return roleService.deleteRole(role_name).map(
                unused -> ApiResponse.<Void>builder().build()
        );
    }

    @DeleteMapping("/delete-all")
    Mono<ApiResponse<Void>> deleteAllRole() {
        return roleService.deleteAllRole().map(
                unused -> ApiResponse.<Void>builder().build()
        );
    }


}