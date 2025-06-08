package com.dauducbach.feed_service.service;

import com.dauducbach.event.PostCreationEvent;
import com.dauducbach.event.PostEvent;
import com.dauducbach.feed_service.entity.FeedHistory;
import com.dauducbach.feed_service.repository.FeedHistoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class FanOutService {
    ReactiveRedisTemplate<String, String> redisTemplate;
    KafkaSender<String, PostEvent> kafkaSender;
    IdGenerator idGenerator;
    FeedHistoryRepository feedHistoryRepository;

    public Mono<Void> handlePostCreate(PostCreationEvent event) {
        return Flux.fromIterable(event.getRecipientId())
                .flatMap(username -> {
                    FeedHistory feedHistory = FeedHistory.builder()
                            .id(String.valueOf(idGenerator.nextId()))
                            .postId(event.getPostId())
                            .username(username)
                            .timestamp(Instant.now().toEpochMilli())
                            .build();

                    return redisTemplate.opsForZSet()
                            .add("feed:" + username, event.getPostId(), event.getTimestamp().toEpochMilli())
                            .doOnSuccess(aBoolean -> log.info("Pushed post {} to {}'s feed", event.getPostId(), username))
                            .doOnError(ex -> log.info("Error while push post to friends: {}", ex.getMessage()))
                            .then(feedHistoryRepository.save(feedHistory));
                })
                .then()
                .then(Mono.fromCallable(() -> {
                    PostEvent postEvent = PostEvent.builder()
                            .title("Thông báo mới từ Social App")
                            .content(event.getAuthorName() + "vừa đăng 1 bài viết. Hãy cho anh ấy biết cảm xúc của bạn nhé!")
                            .recipientId(event.getRecipientId())
                            .build();
                    ProducerRecord<String, PostEvent> producerRecord = new ProducerRecord<>("notification_post", postEvent);
                    SenderRecord<String, PostEvent, String> senderRecord = SenderRecord.create(producerRecord, event.getAuthorName());
                    return kafkaSender.send(Mono.just(senderRecord));
                }))
                .then();
    }
}



