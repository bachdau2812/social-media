package com.dauducbach.api_gateway.configuration;

import com.dauducbach.api_gateway.dto.request.IntrospectRequest;
import com.dauducbach.api_gateway.dto.response.ApiResponse;
import com.dauducbach.api_gateway.dto.response.IntrospectResponse;
import com.dauducbach.api_gateway.service.RateLimiterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    WebClient webClient;
    RateLimiterService rateLimiterService;

    @NonFinal
    private String[] publicEndpoints = {
            "/identity/auth/.*",
            "/identity/users/registration",
    };

    @Value("${app.api-prefix}")
    @NonFinal
    private String apiPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if(isPublicEndpoint(exchange.getRequest())) {
            String path = exchange.getRequest().getURI().getRawPath();
            return chain.filter(exchange);
        }

        String token = extractToken(exchange);
        String endpoint = exchange.getRequest().getURI().getRawPath();

        return webClient.post()
                .uri("http://localhost:8080/identity/auth/introspect")
                .bodyValue(IntrospectRequest.builder()
                        .token(token)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> Mono.error(new RuntimeException("Invalid token")))
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<IntrospectResponse>>() {})
                .map(ApiResponse::getResult)
                .map(IntrospectResponse::isValid)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new RuntimeException("Invalid Token"));
                    } else {
                        String username = extractUsernameFromToken(token); // "admin"
                        List<GrantedAuthority> authorities = extractAuthoritiesFromToken(token);
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities
                        );

                        // Tạo exchange mới với Principal
                        ServerWebExchange authenticatedExchange = exchange.mutate()
                                .principal(Mono.just(authentication))
                                .build();


                        return rateLimiterService.isAllowedSlidingWindow(authenticatedExchange, endpoint)
                                .flatMap(isAllowed -> {
                                    if (isAllowed) {
                                        log.info("Allowed");
                                        return chain.filter(exchange);
                                    } else {
                                        ServerHttpResponse response = exchange.getResponse();
                                        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                                        log.info("Not Allowed");
                                        return response.setComplete();
                                    }
                                });
                    }
                });
    }

    private String extractUsernameFromToken(String token) {
        // Giải mã payload JWT
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        try {
            return new ObjectMapper().readTree(payload).get("sub").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<GrantedAuthority> extractAuthoritiesFromToken(String token) {
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        String scope = null;
        try {
            scope = new ObjectMapper().readTree(payload).get("scope").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return Arrays.stream(scope.split(","))
                .map(role -> new SimpleGrantedAuthority(role.trim()))
                .collect(Collectors.toList());
    }

    private String extractToken (ServerWebExchange serverWebExchange) {
        String authHeader = serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        return authHeader.substring(7);
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        return Arrays.stream(publicEndpoints)
                .anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
    }

    @Override
    public int getOrder() {
        return -1;
    }

}