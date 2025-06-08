package com.dauducbach.identity_service.controller;


import com.dauducbach.identity_service.dto.request.AuthenticationRequest;
import com.dauducbach.identity_service.dto.request.IntrospectRequest;
import com.dauducbach.identity_service.dto.request.LogoutRequest;
import com.dauducbach.identity_service.dto.request.RefreshRequest;
import com.dauducbach.identity_service.dto.response.ApiResponse;
import com.dauducbach.identity_service.dto.response.AuthenticationResponse;
import com.dauducbach.identity_service.dto.response.IntrospectResponse;
import com.dauducbach.identity_service.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/token")
    Mono<ApiResponse<AuthenticationResponse>> authenticate(@RequestBody AuthenticationRequest request) {
        return authenticationService.authenticate(request).map(
                token -> ApiResponse.<AuthenticationResponse>builder()
                        .result(token)
                        .build()
        );
    }

    @PostMapping("/introspect")
    Mono<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request) {
        return authenticationService.introspect(request).map(
                response -> ApiResponse.<IntrospectResponse>builder()
                        .result(response)
                        .build()
        );
    }

    @PostMapping("/refresh")
    Mono<ApiResponse<AuthenticationResponse>> refreshToken(@RequestBody RefreshRequest request) {
        return authenticationService.refreshToken(request).map(
                authenticationResponse -> ApiResponse.<AuthenticationResponse>builder()
                        .result(authenticationResponse)
                        .build()
        );
    }

    @PostMapping("/logout")
    Mono<ApiResponse<Void>> logout (@RequestBody LogoutRequest request) {
        return authenticationService.logout(request).map(
                authenticationResponse -> ApiResponse.<Void>builder()
                        .build()
        );
    }

}