package com.dauducbach.identity_service.controller;

import com.dauducbach.identity_service.dto.request.UserCreationRequest;
import com.dauducbach.identity_service.dto.request.UserUpdateRequest;
import com.dauducbach.identity_service.dto.response.ApiResponse;
import com.dauducbach.identity_service.dto.response.UserResponse;
import com.dauducbach.identity_service.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

@RestController
@RequestMapping("/users")
public class UserController {
    UserService userService;

    @PostMapping("/registration")
    Mono<ApiResponse<UserResponse>> createUser(@RequestBody UserCreationRequest request) {
        return userService.createUser(request).map(
                userResponse -> ApiResponse.<UserResponse>builder()
                        .result(userResponse)
                        .build()
        );
    }

    @PutMapping("/{user_id}")
    Mono<ApiResponse<UserResponse>> updateUser(@PathVariable String user_id, @RequestBody UserUpdateRequest request) {
        return userService.updateUser(Long.parseLong(user_id), request).map(
                userResponse -> ApiResponse.<UserResponse>builder()
                        .result(userResponse)
                        .build()
        );
    }

    @GetMapping
    Flux<ApiResponse<UserResponse>> getAllUser(ServerWebExchange exchange) {
        return userService.getAllUser(exchange).map(
                userResponse -> ApiResponse.<UserResponse>builder()
                        .result(userResponse)
                        .build()
        );
    }

    @GetMapping("/get-user-id/{id}")
    Mono<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        return userService.getUserById(Long.parseLong(id)).map(
                userResponse -> ApiResponse.<UserResponse>builder()
                        .result(userResponse)
                        .build()
        );
    }

    @GetMapping("/my-info")
    Mono<ApiResponse<UserResponse>> getMyInfo() {
        return userService.getMyInfo().map(
                userResponse -> ApiResponse.<UserResponse>builder()
                        .result(userResponse)
                        .build()
        );
    }

    @DeleteMapping("/{user_id}")
    Mono<ApiResponse<Void>> deleteUser(@PathVariable String user_id, ServerWebExchange exchange) {
        return userService.deleteUser(Long.parseLong(user_id), exchange).map(
                unused -> ApiResponse.<Void>builder()
                        .build()
        );
    }

    @DeleteMapping("/delete-all")
    Mono<ApiResponse<Void>> deleteAllUser(ServerWebExchange exchange) {
        return userService.deleteAllUser(exchange).map(
                unused -> ApiResponse.<Void>builder()
                        .build()
        );
    }

    @GetMapping("/get-user-username/{username}")
    Mono<ApiResponse<UserResponse>> findUserByUserName(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(userResponse -> ApiResponse.<UserResponse>builder()
                        .result(userResponse)
                        .build());
    }
}