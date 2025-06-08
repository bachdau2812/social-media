package com.dauducbach.search_service.service;

import co.elastic.clients.elasticsearch._types.SortOptionsBuilders;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.dauducbach.search_service.dto.request.PostIndex;
import com.dauducbach.search_service.dto.request.ProfileIndex;
import com.dauducbach.search_service.dto.request.SearchQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j

public class SearchAutocompleteService {
    private final ReactiveElasticsearchOperations elasticsearchOperations;

    public Mono<Set<String>> getSuggestionQuery(String query) {
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .matchPhrasePrefix(mp -> mp
                                                .field("query")
                                                .query(query)
                                        )
                                )
                        )
                )
                .withSort(SortOptionsBuilders.field(f -> f.field("score").order(SortOrder.Desc)))
                .withPageable(Pageable.ofSize(30))
                .build();

        return elasticsearchOperations.search(nativeQuery, SearchQuery.class)
                .doOnSubscribe(subscription -> log.info("Start search in search_query index"))
                .onErrorResume(e -> {
                    log.error("Search failed: 1", e);
                    return Flux.empty();
                })
                .sort((h1, h2) -> Float.compare(h2.getScore(), h1.getScore()))
                .doOnNext(searchQuerySearchHit -> log.info("Sort!"))
                .map(SearchHit::getContent)
                .doOnNext(searchQuery -> log.info("Check exists complete"))
                .map(SearchQuery::getQuery)
                .collectList()
                .map(HashSet::new)
                .flatMap(listQuery -> {
                    if (listQuery.isEmpty()) {
                        SearchQuery searchQuery = SearchQuery.builder()
                                .query(query)
                                .score(1)
                                .build();
                        elasticsearchOperations.save(searchQuery).subscribe();
                        return Mono.empty();
                    }
                    if (listQuery.size() == 30) {
                        return Mono.just(listQuery);
                    }
                    return addQuery(query)
                            .doOnSuccess(list -> log.info("List post and profile: {}", list))
                            .flatMap(listAddQuery -> {
                                if (!listAddQuery.isEmpty()) {
                                    for (int i = 0 ; i < Math.min(30 - listQuery.size(), listAddQuery.size()) ; i++) {
                                        SearchQuery searchQuery = SearchQuery.builder()
                                                .query(listAddQuery.get(i))
                                                .score(1)
                                                .build();
                                        int finalI = i;
                                        elasticsearchOperations.exists(listAddQuery.get(i), SearchQuery.class)
                                                        .map(exists -> {
                                                            if (exists) {
                                                                return elasticsearchOperations.get(listAddQuery.get(finalI), SearchQuery.class)
                                                                        .map(searchQuery1 -> {
                                                                            searchQuery1.setScore(searchQuery1.getScore() + 1);
                                                                            return searchQuery1;
                                                                        });
                                                            }
                                                            return Mono.empty();
                                                        }).subscribe();
                                        listQuery.add(listAddQuery.get(i));
                                    }


                                }

                                return Mono.just(listQuery);
                            });
                });
    }

    public Mono<List<String>> addQuery(String query) {
        return getSuggestionPost(query)
                .flatMap(listPostQuery -> getSuggestionProfile(query)
                        .map(listProfileQuery -> {
                            listPostQuery.addAll(listProfileQuery);
                            return listPostQuery;
                        })
                );
    }

    public Mono<List<String>> getSuggestionPost(String prefix) {
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(s -> s
                                        .matchPhrasePrefix(m -> m
                                                .field("tags")
                                                .query(prefix)
                                        )
                                )
                        )
                )
                .withPageable(Pageable.ofSize(10))
                .build();

        return elasticsearchOperations.search(nativeQuery, PostIndex.class)
                .onErrorResume(e -> {
                    log.error("Search failed: 2", e);
                    return Flux.empty();
                })
                .sort((hit1, hit2) -> Float.compare(hit2.getScore(), hit1.getScore()))
                .map(SearchHit::getContent)
                .map(postIndex -> {
                    List<String> matchedFields = new ArrayList<>();

                    if (postIndex.getTags() != null) {
                        for (String tag : postIndex.getTags()) {
                            if (tag.contains(prefix)) {
                                matchedFields.add(tag);
                            }
                        }
                    }

                    return matchedFields;
                })
                .flatMapSequential(Flux::fromIterable)
                .collectList();
    }

    public Mono<List<String>> getSuggestionProfile(String query) {
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .should(s -> s.matchPhrasePrefix(m -> m.field("username").query(query)))
                                .should(s -> s.matchPhrasePrefix(m -> m.field("city").query(query)))
                                .should(s -> s.matchPhrasePrefix(m -> m.field("vehicle").query(query)))
                                .should(s -> s.matchPhrasePrefix(m -> m.field("interests").query(query)))
                                .should(s -> s.matchPhrasePrefix(m -> m.field("job").query(query)))
                                .should(s -> s.matchPhrasePrefix(m -> m.field("followPage").query(query)))
                                .should(s -> s.matchPhrasePrefix(m -> m.field("followUser").query(query)))
                        )
                )
                .withPageable(Pageable.ofSize(10))
                .build();

        return elasticsearchOperations.search(nativeQuery, ProfileIndex.class)
                .doOnSubscribe(subscription -> log.info("Start search in search_query index"))
                .onErrorResume(e -> {
                    log.error("Search failed: 3", e);
                    return Flux.empty();
                })
                .sort((hit1, hit2) -> Float.compare(hit2.getScore(), hit1.getScore()))
                .doOnNext(profileIndexSearchHit -> log.info("Sort!"))
                .map(SearchHit::getContent)
                .doOnNext(profileIndex -> log.info("Profile : {}", profileIndex))
                .map(profileIndex -> {
                    List<String> matchedFields = new ArrayList<>();

                    if (profileIndex.getUsername().toLowerCase().contains(query.toLowerCase())) {
                        matchedFields.add(profileIndex.getUsername());
                    }

                    if (profileIndex.getCity().toLowerCase().contains(query.toLowerCase())) {
                        matchedFields.add(profileIndex.getCity());
                    }

                    if (profileIndex.getVehicle().toLowerCase().contains(query.toLowerCase())) {
                        matchedFields.add(profileIndex.getVehicle());
                    }

                    if (profileIndex.getJob().toLowerCase().contains(query.toLowerCase())) {
                        matchedFields.add(profileIndex.getJob());
                    }

                    if (profileIndex.getInterests() != null) {
                        for (String interest : profileIndex.getInterests()) {
                            if (interest.toLowerCase().contains(query.toLowerCase())) {
                                matchedFields.add(interest);
                            }
                        }
                    }

                    if (profileIndex.getFollowPage() != null) {
                        for (String page : profileIndex.getFollowPage()) {
                            if (page.toLowerCase().contains(query.toLowerCase())) {
                                matchedFields.add(page);
                            }
                        }
                    }

                    if (profileIndex.getFollowUser() != null) {
                        for (String user : profileIndex.getFollowUser()) {
                            if (user.toLowerCase().contains(query.toLowerCase())) {
                                matchedFields.add(user);
                            }
                        }
                    }

                    return matchedFields;
                })
                .flatMapSequential(Flux::fromIterable)
                .collectList()
                .doOnSuccess(list -> log.info("List suggest: {}", list));
    }
}
