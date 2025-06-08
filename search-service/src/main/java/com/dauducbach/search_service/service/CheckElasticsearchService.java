package com.dauducbach.search_service.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.dauducbach.search_service.dto.request.PostIndex;
import com.dauducbach.search_service.dto.request.ProfileIndex;
import com.dauducbach.search_service.dto.request.SearchQuery;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class CheckElasticsearchService {
    ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    public Flux<PostIndex> getAllPostIndex () {
        Query matchAll = Query.of(q -> q.matchAll(m -> m));
        NativeQuery nativeQuery = new NativeQueryBuilder()
                .withQuery(matchAll)
                .build();

        return reactiveElasticsearchOperations.search(nativeQuery, PostIndex.class)
                .map(SearchHit::getContent);
    }

    public Flux<ProfileIndex> getAllProfileIndex () {
        Query matchAll = Query.of(q -> q.matchAll(m -> m));
        NativeQuery nativeQuery = new NativeQueryBuilder()
                .withQuery(matchAll)
                .withPageable(Pageable.ofSize(100))
                .build();

        return reactiveElasticsearchOperations.search(nativeQuery, ProfileIndex.class)
                .map(SearchHit::getContent);
    }

    public Flux<SearchQuery> getAllSearchIndex () {
        Query matchAll = Query.of(q -> q.matchAll(m -> m));
        NativeQuery nativeQuery = new NativeQueryBuilder()
                .withQuery(matchAll)
                .withPageable(Pageable.ofSize(100))
                .build();

        return reactiveElasticsearchOperations.search(nativeQuery, SearchQuery.class)
                .map(SearchHit::getContent);
    }

    public Mono<String> deleteAllSearchIndex(){
        Query matchAll = Query.of(q -> q.matchAll(m -> m));

        return reactiveElasticsearchOperations
                .delete(String.valueOf(matchAll), PostIndex.class)
                .doOnNext(count -> log.info("Đã xóa toàn bộ {} documents", count));
    }
}
