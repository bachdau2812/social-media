package com.dauducbach.feed_service.controller;

import com.dauducbach.feed_service.dto.response.PostResponse;
import com.dauducbach.feed_service.service.BuildFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class FeedController {
    private final BuildFeedService buildFeedService;

    @GetMapping
    Flux<PostResponse> getFeed(@RequestParam String username, @RequestParam double score, @RequestParam int limit) {
        return buildFeedService.getFeed(username, score, limit);
    }
}
