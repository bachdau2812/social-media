package com.dauducbach.notification_service.service;

import com.dauducbach.event.NotificationEvent;
import com.dauducbach.notification_service.dto.request.PushNotificationRequest;
import com.dauducbach.notification_service.entity.NotificationDB;
import com.dauducbach.notification_service.repository.NotificationRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;

import java.time.Duration;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j

public class NotificationService {
    SmsService smsService;
    EmailService emailService;
    KafkaSender<String, NotificationEvent> kafkaSender;
    ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    IdGenerator idGenerator;
    NotificationRepository notificationRepository;

    public Mono<String> sendNotification(NotificationEvent event) {
        switch (event.getChanel()){
            case "PUSH":
                PushNotificationRequest request = PushNotificationRequest.builder()
                        .deviceToken(event.getRecipient())
                        .title(event.getSubject())
                        .body(event.getBody())
                        .build();

                return pushNotification(request);
            case "EMAIL":
                return emailService.sendEmail(event);
            case "SMS":
                return smsService.smsNotification(event);
            default:
                return Mono.just("Unsupported chanel: " + event.getChanel());
        }
    }

    public Mono<String> pushNotification(PushNotificationRequest request) {
        String collapseKey = DigestUtils.md5DigestAsHex((request.getTitle() + request.getBody()).getBytes());

        return isDuplicate(collapseKey)
                .flatMap(duplicate -> {
                    if (duplicate) {
                        log.warn("[PushNotification] Duplicate notification detected for key: {}", collapseKey);
                        return Mono.error(new RuntimeException("Notification already sent"));
                    }

                    Notification notification = Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getBody())
                            .build();

                    MulticastMessage multicastMessage = MulticastMessage.builder()
                            .addAllTokens(request.getDeviceToken())
                            .setNotification(notification)
                            .build();

                    NotificationDB notificationDB = NotificationDB.builder()
                            .id(idGenerator.nextId())
                            .timestamp(LocalDate.now())
                            .deviceToken(request.getDeviceToken())
                            .title(request.getTitle())
                            .body(request.getBody())
                            .build();

                    return Mono.fromCallable(() -> FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage))
                            .flatMap(batchResponse -> {
                                log.info("[PushNotification] Success! Message ID: {}, CollapseKey: {}", batchResponse, collapseKey);

                                return notificationRepository.save(notificationDB)
                                        .doOnSuccess(saved -> log.info("Save complete notification: {}", saved))
                                        .doOnError(e -> log.error("Error while save notification: {}", e.getMessage()))
                                        .thenReturn("Số thông báo gửi thành công: " + batchResponse.getSuccessCount());
                            });
                })
                .onErrorResume(e -> {
                    log.error("[PushNotification] Failed to send notification. CollapseKey: {}", collapseKey, e);
                    return Mono.error(new RuntimeException("Failed to send notification: " + e.getMessage()));
                });
    }

    public Mono<Boolean> isDuplicate(String collapseKey) {
        return reactiveRedisTemplate.opsForValue()
                .setIfAbsent("notif:lock:" + collapseKey, "1", Duration.ofHours(1))
                .map(Boolean.FALSE::equals);
    }


}
