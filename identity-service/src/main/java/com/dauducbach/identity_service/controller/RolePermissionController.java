package com.dauducbach.identity_service.controller;

import com.dauducbach.identity_service.dto.request.AddPermissionForRoleRequest;
import com.dauducbach.identity_service.dto.request.DeletePermissionOfRoleRequest;
import com.dauducbach.identity_service.dto.response.ApiResponse;
import com.dauducbach.identity_service.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

@RestController
@RequestMapping("/edit-role")
public class RolePermissionController {

    RoleService roleService;

    @PostMapping("/{role_name}")
    Mono<ApiResponse<Void>> addPermission(@PathVariable String role_name, @RequestBody AddPermissionForRoleRequest request) {
        return roleService.addPermission(request, role_name).map(
                unused -> ApiResponse.<Void>builder().build()
        );
    }

    @DeleteMapping("/{role_name}")
    Mono<ApiResponse<Void>> deletePermission(@PathVariable String role_name,@RequestBody DeletePermissionOfRoleRequest request) {
        return roleService.deletePermission(request, role_name).map(
                unused -> ApiResponse.<Void>builder().build()
        );
    }
}