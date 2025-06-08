package com.dauducbach.search_service.service;

import com.dauducbach.search_service.dto.request.GlobalSearch;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class GlobalSearchService {
    PostSearchService postSearchService;
    ProfileSearchService profileSearchService;

    public Mono<GlobalSearch> fullTextSearch(String query) {
        return postSearchService.fullTextSearch(query)
                .collectList()
                .map(postResponses -> GlobalSearch.builder()
                        .listPost(postResponses)
                        .build()
                )
                .flatMap(globalSearch1 -> profileSearchService.fullTextSearch(query)
                        .collectList()
                        .map(profileResponses -> {
                            globalSearch1.setListProfile(profileResponses);
                            return globalSearch1;
                        })
                );
    }

    public Mono<GlobalSearch> semanticSearch(String query) {
        return postSearchService.semanticSearch(query)
                .collectList()
                .map(postResponses -> GlobalSearch.builder()
                        .listPost(postResponses)
                        .build()
                )
                .flatMap(globalSearch1 -> profileSearchService.semanticSearch(query)
                        .collectList()
                        .map(profileResponses -> {
                            globalSearch1.setListProfile(profileResponses);
                            return globalSearch1;
                        })
                );
    }
}
