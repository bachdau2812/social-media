package com.dauducbach.post_service.service;

import com.dauducbach.event.PostCreationEvent;
import com.dauducbach.event.PostEvent;
import com.dauducbach.post_service.dto.request.EmbeddingRequest;
import com.dauducbach.post_service.dto.request.PostCreateRequest;
import com.dauducbach.post_service.dto.request.PostUpdateRequest;
import com.dauducbach.post_service.dto.response.EmbeddingResponse;
import com.dauducbach.post_service.dto.response.PostResponse;
import com.dauducbach.post_service.dto.response.ProfileResponse;
import com.dauducbach.post_service.entity.Post;
import com.dauducbach.post_service.entity.PostIndex;
import com.dauducbach.post_service.mapper.PostMapper;
import com.dauducbach.post_service.repository.ActionRepository;
import com.dauducbach.post_service.repository.PostRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Instant;
import java.util.HashSet;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class PostService {
    WebClient webClient;
    PostRepository postRepository;
    PostMapper postMapper;
    ReactiveElasticsearchOperations reactiveElasticsearchOperations;
    IdGenerator idGenerator;
    ActionRepository actionRepository;
    KafkaSender<String, PostCreationEvent> kafkaSender;

    public Mono<PostResponse> create(PostCreateRequest request) {
        var post = postMapper.toPost(request);

        post.setId(String.valueOf(idGenerator.nextId()));
        post.setCreateAt(Instant.now());
        post.setUpdateAt(Instant.now());
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);

        return setLocation(request)
                .flatMap(location -> {
                    post.setGeoLocation(location);
                    return webClient.post()
                            .uri("http://localhost:5000/embed")
                            .bodyValue(EmbeddingRequest.builder()
                                    .text(prepareEmbeddingInput(post))
                                    .build())
                            .retrieve()
                            .bodyToMono(EmbeddingResponse.class)
                            .doOnError(ex -> log.info("Error while get embedding: {}", ex.getMessage()))
                            .flatMap(response -> {
                                post.setEmbedding(response.getEmbedding());
                                return postRepository.save(post);
                            });

                })
                .flatMap(savedPost -> {
                    reactiveElasticsearchOperations.save(postMapper.toPostIndex(savedPost))
                            .doOnError(ex -> log.info("Error while save to elasticsearch: {}", ex.getMessage()))
                            .subscribe();
                    return buildEvent(post)
                            .flatMap(postCreationEvent -> {
                                ProducerRecord<String, PostCreationEvent> producerRecord = new ProducerRecord<>("notification_post_create", postCreationEvent);
                                SenderRecord<String, PostCreationEvent, String> senderRecord = SenderRecord.create(producerRecord, "up_post");
                                return kafkaSender.send(Mono.just(senderRecord))
                                        .doOnSubscribe(subscription -> log.info("Start send: {}", postCreationEvent))
                                        .then(Mono.just(postMapper.toPostResponse1(post)));
                            });
                });
    }


    private Mono<PostCreationEvent> buildEvent(Post post) {
        PostCreationEvent postCreationEvent = PostCreationEvent.builder()
                .postId(post.getId())
                .build();
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    Jwt jwt = (Jwt) principal;
                    return jwt.getTokenValue();
                })
                .flatMap(token -> webClient.get()
                        .uri("http://localhost:8081/profile/my-info")
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(ProfileResponse.class)
                        .doOnError(ex -> log.info("Error while get username: {}", ex.getMessage()))
                        .map(profileResponse -> {
                            postCreationEvent.setAuthorName(profileResponse.getUsername());
                            postCreationEvent.setRecipientId(new HashSet<>(profileResponse.getFriends()));
                            postCreationEvent.setTimestamp(Instant.now());
                            return postCreationEvent;
                        })
                );
    }

    private Mono<String> setLocation(PostCreateRequest post) {
        return webClient.get()
                .uri(uriBuilder ->  uriBuilder
                        .scheme("https")
                        .host("nominatim.openstreetmap.org")
                        .path("reverse")
                        .queryParam("lat", post.getLocation().get(0))
                        .queryParam("lon", post.getLocation().get(1))
                        .queryParam("format", "json")
                        .build()
                )
                .header(HttpHeaders.USER_AGENT, "PostServiceApplication")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.get("display_name").asText());
    }

    private String prepareEmbeddingInput(Post post) {
        return String.join("\n",
                "Content: " + post.getContent(),
                "Tags: " + String.join(", ", post.getTags()),
                "GeoLocation: " + String.join(", ", post.getGeoLocation())
        );
    }

    public Mono<PostResponse> updatePost(String id, PostUpdateRequest request){
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Post not found with id: " + id)))
                .flatMap(post -> {
                    post.setContent(request.getContent());
                    post.setMediaUrls(request.getMediaUrls());
                    post.setVisibility(request.getVisibility());
                    post.setTags(request.getTags());
                    post.setUpdateAt(Instant.now());

                    return postRepository.save(post)
                            .map(postMapper::toPostResponse1)
                            .doOnSuccess(post1 -> {
                                var index = postMapper.toPostIndex(post);
                                reactiveElasticsearchOperations.save(postMapper.toPostIndex(post)).subscribe();
                            })
                            ;
                });
    }

    public Flux<PostResponse> getAllPostByUserId(String userId) {
        return postRepository.findAllByUserId(userId)
                .map(postMapper::toPostResponse1)
                .onErrorResume(ex -> Mono.error(new RuntimeException(ex.getMessage())));
    }

    public Mono<Void> deletePost(String postId) {
        return postRepository.deleteById(postId)
                .onErrorResume(ex -> Mono.error(new RuntimeException(ex.getMessage())))
                .then(actionRepository.deleteAllByPostId(postId))
                .then(Mono.just(reactiveElasticsearchOperations.delete(postId, PostIndex.class)))
                .then();
    }

    public Mono<Void> deleteAllPostByUserId(String userId){
        return postRepository.findAllByUserId(userId)
                .flatMap(post -> {
                    actionRepository.deleteAllByPostId(post.getId());
                    reactiveElasticsearchOperations.delete(post.getId(), PostIndex.class).subscribe();
                    return Mono.empty();
                })
                .then(postRepository.deleteAllByUserId(userId));
    }

    public Mono<Void> deleteAll() {
        return postRepository.findAll()
                .flatMap(post -> reactiveElasticsearchOperations.delete(post.getId(), PostIndex.class))
                .then(postRepository.deleteAll());
    }

    public Mono<PostResponse> getPost(String postId) {
        return postRepository.findById(postId)
                .map(postMapper::toPostResponse1)
                .onErrorResume(ex -> Mono.error(new RuntimeException(ex.getMessage())));
    }

}
