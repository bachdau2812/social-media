package com.dauducbach.profile_service.service;

import com.dauducbach.event.ProfileCreationEvent;
import com.dauducbach.profile_service.dto.request.ProfileCreationRequest;
import com.dauducbach.profile_service.mapper.ProfileMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class ListenerTopicService {
    ProfileService profileService;
    ProfileMapper profileMapper;

    @KafkaListener(topics = "profile-creation")
    public void createProfile(@Payload ProfileCreationEvent event) {
        log.info("Profile creation event: {}", event);
        profileService.create(profileMapper.toProfileCreationRequest(event))
                .doOnSuccess(profileResponse -> log.info("Create Profile Conplete: {}", profileResponse))
                .doOnError(e -> log.info("Create profile complete"))
                .subscribe();
    }
}
