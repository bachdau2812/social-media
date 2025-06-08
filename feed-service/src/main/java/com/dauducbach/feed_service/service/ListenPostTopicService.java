package com.dauducbach.feed_service.service;

import com.dauducbach.event.PostCreationEvent;
import com.dauducbach.event.PostEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ListenPostTopicService {
    FanOutService fanOutService;

    @KafkaListener(topics = "notification_post_create")
    public void handle(@Payload PostCreationEvent event) {
        fanOutService.handlePostCreate(event)
                .doOnSuccess(unused -> log.info("Xu ly thanh cong fanout bai viet"))
                .doOnError(ex -> log.info("Error while fanout post: {}", ex.getMessage()))
                .subscribe();
    }
}
