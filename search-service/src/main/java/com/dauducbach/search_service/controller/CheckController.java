package com.dauducbach.search_service.controller;

import com.dauducbach.search_service.dto.request.PostIndex;
import com.dauducbach.search_service.dto.request.ProfileIndex;
import com.dauducbach.search_service.dto.request.SearchQuery;
import com.dauducbach.search_service.service.CheckElasticsearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class CheckController {
    CheckElasticsearchService checkElasticsearchService;

    @GetMapping("/all-post")
    public Flux<PostIndex> getAllPostIndex() {
        return checkElasticsearchService.getAllPostIndex();
    }

    @GetMapping("/all-profile")
    public Flux<ProfileIndex> getAllProfile() {
        return checkElasticsearchService.getAllProfileIndex();
    }

    @GetMapping("/all-search")
    public Flux<SearchQuery> getAllSearch() {
        return checkElasticsearchService.getAllSearchIndex();
    }
}
