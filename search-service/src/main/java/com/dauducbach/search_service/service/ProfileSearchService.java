package com.dauducbach.search_service.service;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.json.JsonData;
import com.dauducbach.search_service.dto.request.EmbeddingRequest;
import com.dauducbach.search_service.dto.request.ProfileIndex;
import com.dauducbach.search_service.dto.response.EmbeddingResponse;
import com.dauducbach.search_service.dto.response.ProfileResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProfileSearchService {
    ReactiveElasticsearchOperations reactiveElasticsearchOperations;
    WebClient webClient;

    public Flux<ProfileResponse> fullTextSearch(String query) {
        HighlightQuery highlightQuery = new HighlightQuery(
                new Highlight(List.of(
                        new HighlightField("username"),
                        new HighlightField("firstName"),
                        new HighlightField("lastName"),
                        new HighlightField("city"),
                        new HighlightField("job"),
                        new HighlightField("interests"),
                        new HighlightField("vehicle")
                )),
                ProfileIndex.class
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .query(query)
                                .fields(
                                        "username",
                                        "firstName",
                                        "lastName",
                                        "city",
                                        "job",
                                        "vehicle",
                                        "interests"
                                )
                                .type(TextQueryType.BestFields)
                                .fuzziness("AUTO")
                                .minimumShouldMatch("100%")
                        )
                )
                .withHighlightQuery(highlightQuery)
                .withPageable(Pageable.ofSize(10))
                .build();

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(Jwt.class)
                .map(Jwt::getTokenValue)
                .flatMapMany(token ->
                        reactiveElasticsearchOperations.search(nativeQuery, ProfileIndex.class)
                                .doOnSubscribe(subscription -> log.info("Start get ProfileResponse"))
                                .flatMapSequential(profileIndex -> webClient.get()
                                        .uri("http://localhost:8081/profile/{profileId}", profileIndex.getId())
                                        .header("Authorization", "Bearer " + token)
                                        .retrieve()
                                        .bodyToMono(ProfileResponse.class)
                                        .doOnSuccess(profileResponse -> log.info("Profile: {}", profileResponse))
                                )
                );
    }

    public Flux<ProfileResponse> semanticSearch(String query) {
        return webClient.post()
                .uri("http://localhost:5000/embed")
                .bodyValue(EmbeddingRequest.builder()
                        .text(query)
                        .build())
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .map(EmbeddingResponse::getEmbedding)
                .flatMapMany(embeddings -> {
                    NativeQuery nativeQuery = NativeQuery.builder()
                            .withQuery(q -> q
                                    .scriptScore(ss -> ss
                                            .query(innerQuery -> innerQuery
                                                    .matchAll(m -> m)
                                            )
                                            .script(script -> script
                                                    .source("cosineSimilarity(params.query_vector, 'embeddings') + 1.0")
                                                    .params(Map.of("query_vector", JsonData.of(embeddings))
                                                    )
                                            )
                                    )
                            )
                            .withPageable(Pageable.ofSize(10))
                            .build();

                    return ReactiveSecurityContextHolder.getContext()
                            .doOnNext(securityContext -> log.info("Get context complete"))
                            .map(SecurityContext::getAuthentication)
                            .doOnNext(authentication -> log.info("Get authentication complete"))
                            .map(Authentication::getPrincipal)
                            .doOnNext(o -> log.info("Get principal complete"))
                            .cast(Jwt.class)
                            .map(Jwt::getTokenValue)
                            .doOnSuccess(s -> log.info("Token : {}", s))
                            .flatMapMany(token ->
                                    reactiveElasticsearchOperations.search(nativeQuery, ProfileIndex.class)
                                            .doOnSubscribe(subscription -> log.info("Start get ProfileResponse"))
                                            .flatMapSequential(profileIndex -> webClient.get()
                                                    .uri("http://localhost:8081/profile/{profileId}", profileIndex.getId())
                                                    .header("Authorization", "Bearer " + token)
                                                    .retrieve()
                                                    .bodyToMono(ProfileResponse.class)
                                                    .doOnSuccess(profileResponse -> log.info("Profile: {}", profileResponse))
                                            )
                            );
                });
    }
}






















