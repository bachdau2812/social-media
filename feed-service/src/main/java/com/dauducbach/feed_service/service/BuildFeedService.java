package com.dauducbach.feed_service.service;

import com.dauducbach.feed_service.dto.response.ApiResponse;
import com.dauducbach.feed_service.dto.response.PostResponse;
import com.dauducbach.feed_service.entity.FeedHistory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Range;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BuildFeedService {
    ReactiveRedisTemplate<String, String> redisTemplate;
    WebClient webClient;
    ReactiveMongoTemplate reactiveMongoTemplate;

    public Flux<PostResponse> getFeed(String username, double lastScore, int limit) {
        Range<Double> scoreRange = Range.closed(0.0, (double) lastScore - 1);

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    Jwt jwt = (Jwt) principal;
                    return jwt.getTokenValue();
                })
                .flatMapMany(token -> createFeed(username, lastScore, limit, token));
    }

    private Flux<PostResponse> fallBack(String username, int limit, String token) {
        Criteria criteria = Criteria.where("username").is(username);

        Query query = new Query(criteria);
        query.limit(limit);

        return reactiveMongoTemplate.find(query, FeedHistory.class)
                .flatMapSequential(feedHistory -> getPost(feedHistory.getPostId(), token));
    }

    private Mono<PostResponse> getPost(String postId,String token) {
        return webClient.get()
                .uri("http://localhost:8084/post/{postId}", postId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PostResponse>>() {
                })
                .map(ApiResponse::getResult);
    }

    private Flux<PostResponse> createFeed(String username, double lastScore, int limit, String token) {
        String key = "feed:" + username;
        Range<Double> range = Range.closed(0.0, lastScore);

        return redisTemplate.opsForZSet().size(key)
                .flatMapMany(size -> {
                    if (size < 3) {
                        log.info("Get feed from fallBack !!");
                        return fallBack(username, limit, token);
                    } else {
                        log.info("Get feed from redis !!");
                        return redisTemplate.opsForZSet().reverseRangeByScore(key, range)
                                .take(limit)
                                .flatMapSequential(postId ->
                                        redisTemplate.opsForZSet().remove(key, postId)
                                                .then(getPost(postId, token))
                                );
                    }
                });
    }


}
