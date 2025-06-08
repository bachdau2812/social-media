package com.dauducbach.notification_service.service;

import com.dauducbach.event.NotificationEvent;
import com.dauducbach.notification_service.dto.request.SmsRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class SmsService {
    WebClient webClient;

    @NonFinal
    @Value("${brevo.api-key}")
    String apiKey;

    public Mono<String> smsNotification(NotificationEvent event) {
        AtomicInteger successCount = new AtomicInteger(0);
        return Flux.fromIterable(event.getRecipient())
                .flatMap(phoneNumber -> {
                    SmsRequest smsRequest = SmsRequest.builder()
                            .sender(event.getSubject())
                            .recipient(phoneNumber)
                            .content(event.getBody())
                            .type("transactional")
                            .build();

                    return webClient.post()
                            .uri("https://api.brevo.com/v3/transactionalSMS/sms")
                            .header("api-key", apiKey)
                            .bodyValue(smsRequest)
                            .retrieve()
                            .bodyToMono(String.class)
                            .doOnSuccess(s -> {
                                int count = successCount.incrementAndGet();
                                log.info("Sent SMS to {} (success count: {})", phoneNumber, count);
                            })
                            .doOnError(e -> Mono.error(new RuntimeException("Send fail: " + e.getMessage())));
                }
                )
                .then(Mono.just("Sms complete count: " + successCount.get()));
    }
}
