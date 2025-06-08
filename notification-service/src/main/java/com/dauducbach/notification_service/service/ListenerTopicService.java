package com.dauducbach.notification_service.service;

import com.dauducbach.event.MessageEvent;
import com.dauducbach.event.NotificationEvent;
import com.dauducbach.event.PostEvent;
import com.dauducbach.notification_service.dto.request.PushNotificationRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

@Service
public class ListenerTopicService {
    WebClient webClient;
    NotificationService notificationService;
    EmailService emailService;

    @KafkaListener(topics = "notification_system")
    public void consumeNotification(@Payload NotificationEvent notificationEvent) {
        emailService.createUserCompleteNotification(notificationEvent).subscribe();
    }

    @KafkaListener(topics = "notification_chat")
    public void getMessageEvent(@Payload MessageEvent messageEvent) {
        PushNotificationRequest pushNotificationRequest = PushNotificationRequest.builder()
                .deviceToken(List.of(
                        "cgFfMhWRde2fjcBmL-WIrn:APA91bG-C-LR6ZT-XkMzZaAJIajouV3F8fcShfNAsVhl8etyeUtsyz-U08PZS40C22UTIhKTUexDjEhw59m7gMeqKcQLJK7IhNqJcyE_nU5MPolDHPylWL8"
                ))
                .title(messageEvent.getMessageFrom())
                .body(messageEvent.getContent())
                .build();
        log.info("Push Notification: {}", pushNotificationRequest);

        notificationService.pushNotification(pushNotificationRequest)
                .doOnSubscribe(subscription -> log.info("Starting send !"))
                .doOnSuccess(s -> log.info("Send complete: {}", s))
                .doOnError(ex -> log.info("Error while send push notification: {}", ex.getMessage()))
                .subscribe();
    }

    @KafkaListener(topics = "notification_post")
    public void sendPostNotification(@Payload PostEvent postEvent) {
        log.info("Post event: {}", postEvent);
        StringBuilder stringBuilder = new StringBuilder(postEvent.getContent());
        stringBuilder.append("\n");
        postEvent.getRecipientId().forEach(stringBuilder::append);

        PushNotificationRequest pushNotificationRequest = PushNotificationRequest.builder()
                .deviceToken(List.of(
                        "cgFfMhWRde2fjcBmL-WIrn:APA91bG-C-LR6ZT-XkMzZaAJIajouV3F8fcShfNAsVhl8etyeUtsyz-U08PZS40C22UTIhKTUexDjEhw59m7gMeqKcQLJK7IhNqJcyE_nU5MPolDHPylWL8"
                ))
                .title(postEvent.getTitle())
                .body(stringBuilder.toString())
                .build();

        log.info("Push notification: {}", pushNotificationRequest);
        notificationService.pushNotification(pushNotificationRequest)
                .doOnSubscribe(subscription -> log.info("Starting send !"))
                .doOnSuccess(s -> log.info("Send complete: {}", s))
                .doOnError(ex -> log.info("Error while send push notification: {}", ex.getMessage()))
                .subscribe();
    }
}
