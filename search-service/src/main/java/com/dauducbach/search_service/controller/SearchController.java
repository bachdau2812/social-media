package com.dauducbach.search_service.controller;

import com.dauducbach.search_service.dto.request.GlobalSearch;
import com.dauducbach.search_service.dto.response.PostResponse;
import com.dauducbach.search_service.dto.response.ProfileResponse;
import com.dauducbach.search_service.service.GlobalSearchService;
import com.dauducbach.search_service.service.PostSearchService;
import com.dauducbach.search_service.service.ProfileSearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class SearchController {
    ProfileSearchService profileSearchService;
    PostSearchService postSearchService;
    GlobalSearchService globalSearchService;

    @GetMapping("/fulltext")
    Mono<GlobalSearch> globalFullText(@RequestParam String query) {
        return globalSearchService.fullTextSearch(query);
    }

    @GetMapping("/semantic")
    Mono<GlobalSearch> globalSemantic(@RequestParam String query) {
        return globalSearchService.semanticSearch(query);
    }

    @GetMapping("/post/fulltext")
    Flux<PostResponse> postFullText(@RequestParam String query) {
        return postSearchService.fullTextSearch(query);
    }

    @GetMapping("/post/semantic")
    Flux<PostResponse> postSemantic(@RequestParam String query) {
        return postSearchService.semanticSearch(query);
    }

    @GetMapping("/profile/fulltext")
    Flux<ProfileResponse> profileFullText(@RequestParam String query) {
        return profileSearchService.fullTextSearch(query);
    }

    @GetMapping("/profile/semantic")
    Flux<ProfileResponse> profileSemantic(@RequestParam String query) {
        return profileSearchService.semanticSearch(query);
    }
}
