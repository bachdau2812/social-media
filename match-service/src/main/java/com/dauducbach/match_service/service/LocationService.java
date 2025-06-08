package com.dauducbach.match_service.service;

import com.dauducbach.match_service.dto.CurrentLocation;
import com.dauducbach.match_service.dto.request.LocationRequest;
import com.dauducbach.match_service.entity.LocationHistory;
import com.dauducbach.match_service.repository.LocationHistoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class LocationService {
    LocationHistoryRepository locationHistoryRepository;
    ReactiveRedisTemplate<String, CurrentLocation> reactiveRedisTemplate;
    WebClient webClient;
    IdGenerator idGenerator;

    public Mono<LocationHistory> saveLocation(LocationRequest request) {
        String redisKey = "location:user:" + request.getUserId();

        return reactiveRedisTemplate.opsForValue().get(redisKey)
                .flatMap(existing -> updateLocation(request))
                .switchIfEmpty(
                        setLocation(request.getLatitude(), request.getLongitude())
                                .flatMap(geoLocation -> {
                                    CurrentLocation currentLocation = CurrentLocation.builder()
                                            .latitude(request.getLatitude())
                                            .longitude(request.getLongitude())
                                            .build();

                                    LocationHistory locationHistory = LocationHistory.builder()
                                            .id(UUID.randomUUID().toString())
                                            .userId(request.getUserId())
                                            .longitude(request.getLongitude())
                                            .latitude(request.getLatitude())
                                            .timestamp(Instant.now())
                                            .deviceName("Lenovo Thinkpad P50")
                                            .geoLocation(geoLocation)
                                            .build();

                                    return reactiveRedisTemplate.opsForValue()
                                            .set(redisKey, currentLocation)
                                            .then(locationHistoryRepository.save(locationHistory))
                                            .thenReturn(locationHistory);
                                })
                );
    }

    public Mono<LocationHistory> updateLocation(LocationRequest request) {
        String redisKey = "location:user:" + request.getUserId();

        return reactiveRedisTemplate.opsForValue().get(redisKey)
                .switchIfEmpty(Mono.error(new RuntimeException("Key không tồn tại")))
                .flatMap(existing -> {
                    CurrentLocation currentLocation = CurrentLocation.builder()
                            .latitude(request.getLatitude())
                            .longitude(request.getLongitude())
                            .build();

                    return setLocation(request.getLatitude(), request.getLongitude())
                            .flatMap(geoLocation -> {
                                LocationHistory locationHistory = LocationHistory.builder()
                                        .id(String.valueOf(idGenerator.nextId()))
                                        .userId(request.getUserId())
                                        .longitude(request.getLongitude())
                                        .latitude(request.getLatitude())
                                        .timestamp(Instant.now())
                                        .deviceName("Lenovo Thinkpad P50")
                                        .geoLocation(geoLocation)
                                        .build();

                                return reactiveRedisTemplate.opsForValue()
                                        .set(redisKey, currentLocation)
                                        .then(locationHistoryRepository.save(locationHistory))
                                        .thenReturn(locationHistory);
                            });
                });
    }


    private Mono<String> setLocation(double latitude, double longitude) {
        return webClient.get()
                .uri(uriBuilder ->  uriBuilder
                        .scheme("https")
                        .host("nominatim.openstreetmap.org")
                        .path("reverse")
                        .queryParam("lat", latitude)
                        .queryParam("lon", longitude)
                        .queryParam("format", "json")
                        .build()
                )
                .header(HttpHeaders.USER_AGENT, "PostServiceApplication")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.get("display_name").asText())
                .onErrorResume(e -> {
                    log.error("Error fetching location: ", e);
                    return Mono.just("Unknown location");
                });
    }

    public Mono<String> removeLocationInRedis(String userId) {
        String redisKey = "location:user:" + userId;

        return reactiveRedisTemplate.opsForValue().delete(redisKey)
                .onErrorResume(e -> {
                    log.error("Error while remove location: ", e);
                    return Mono.empty();
                })
                .then(Mono.just("Xóa vị trí của " + userId + " thành công"));
    }


    public Flux<LocationHistory> findLocationHistoryOfUser (String userId) {
        return locationHistoryRepository.findByUserId(userId);
    }

    public Mono<LocationHistory> lastActive(String userId){
        return findLocationHistoryOfUser(userId)
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.empty();
                    }

                    LocationHistory latest = list.stream()
                            .max(Comparator.comparing(LocationHistory::getTimestamp))
                            .orElseThrow();

                    return Mono.just(latest);
                });
    }
}