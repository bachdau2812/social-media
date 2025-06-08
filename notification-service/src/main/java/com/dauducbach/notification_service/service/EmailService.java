package com.dauducbach.notification_service.service;

import com.dauducbach.event.NotificationEvent;
import com.dauducbach.notification_service.dto.request.EmailRequest;
import com.dauducbach.notification_service.dto.request.Recipient;
import com.dauducbach.notification_service.dto.request.Sender;
import com.dauducbach.notification_service.dto.response.ApiResponse;
import com.dauducbach.notification_service.dto.response.ProfileResponse;
import com.dauducbach.notification_service.dto.response.UserResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j

public class EmailService {
    WebClient webClient;

    @NonFinal
    @Value("${brevo.api-key}")
    String apiKey;

    @NonFinal
    @Value("${brevo.api-url}")
    String apiUrl;

    public Mono<String> sendEmail (NotificationEvent request) {
        log.info("API key: {}", apiKey);
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .doOnNext(principal -> System.out.println("Principal class: " + principal.getClass()))
                .map(principal -> {
                    Jwt jwt = (Jwt) principal;
                    return jwt.getTokenValue();
                })
                .flatMap(token -> Flux.fromIterable(request.getRecipient())
                        .flatMap(userId -> webClient.get()
                                .uri("http://localhost:8080/identity/users/get-user-id/{userId}", userId)
                                .header("Authorization","Bearer " + token)
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserResponse>>() {})
                                .onErrorResume(e -> Mono.error(new RuntimeException(e.getMessage())))
                                .map(ApiResponse::getResult)
                        )
                        .collectList()
                        .flatMap(listUser -> webClient.get()
                                .uri("http://localhost:8081/profile/my-info")
                                .header("Authorization","Bearer " + token)
                                .retrieve()
                                .bodyToMono(ProfileResponse.class)
                                .flatMap(profileResponse -> {
                                    log.info("Recipient: {}", listUser);
                                    EmailRequest emailRequest = EmailRequest.builder()
                                            .sender(Sender.builder()
                                                    .name(profileResponse.getUsername())
                                                    .email(profileResponse.getEmail())
                                                    .build())
                                            .to(listUser.stream()
                                                    .map(userResponse -> Recipient.builder()
                                                            .name(userResponse.getUsername())
                                                            .email(userResponse.getEmail())
                                                            .build())
                                                    .collect(Collectors.toList())
                                            )
                                            .subject(request.getSubject())
                                            .htmlContent(request.getBody())
                                            .build();
                                    log.info("Email recipient: {}", emailRequest.getTo());
                                    return webClient.post()
                                            .uri(apiUrl)
                                            .header("api-key", apiKey)
                                            .bodyValue(emailRequest)
                                            .retrieve()
                                            .bodyToMono(String.class)
                                            .doOnSuccess(s -> log.info("Message: {}", s))
                                            .doOnTerminate(() -> log.info("Email send request processed"))
                                            .doOnError(error -> log.error("Error sending email: ", error));
                                })
                        ));
    }

    public Mono<String> createUserCompleteNotification(NotificationEvent event) {
        Recipient recipient = Recipient.builder()
                .name("Test")
                .email(event.getRecipient().get(0))
                .build();
        return webClient.post()
                .uri(apiUrl)
                .header("api-key", apiKey)
                .bodyValue(EmailRequest.builder()
                        .sender(Sender.builder()
                                .name("SYSTEM")
                                .email("dauhuy19@gmail.com")
                                .build())
                        .to(List.of(recipient))
                        .subject(event.getSubject())
                        .htmlContent(event.getBody())
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(s -> log.info("Message: {}", s))
                .doOnTerminate(() -> log.info("Email send request processed"))
                .doOnError(error -> log.error("Error sending email: ", error));
    }
}
