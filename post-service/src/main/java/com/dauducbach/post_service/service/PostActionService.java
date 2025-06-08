package com.dauducbach.post_service.service;

import com.dauducbach.event.PostEvent;
import com.dauducbach.post_service.constant.ActionType;
import com.dauducbach.post_service.dto.request.ActionRequest;
import com.dauducbach.post_service.dto.request.CommentCreationRequest;
import com.dauducbach.post_service.dto.response.ProfileResponse;
import com.dauducbach.post_service.entity.Comment;
import com.dauducbach.post_service.entity.PostAction;
import com.dauducbach.post_service.mapper.ActionMapper;
import com.dauducbach.post_service.repository.ActionRepository;
import com.dauducbach.post_service.repository.CommentRepository;
import com.dauducbach.post_service.repository.PostRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Instant;
import java.util.HashSet;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service

public class PostActionService {
    ActionRepository actionRepository;
    CommentRepository commentRepository;
    PostRepository postRepository;
    ActionMapper actionMapper;
    WebClient webClient;
    IdGenerator idGenerator;
    KafkaSender<String, PostEvent> kafkaSender;

    public Mono<PostAction> like_and_share(ActionRequest request) {
        PostAction postAction = actionMapper.toPostAction(request);
        postAction.setId(String.valueOf(idGenerator.nextId()));
        postAction.setActionDate(Instant.now());

        return actionRepository.save(postAction)
                .doOnError(ex -> log.info("Error while save action: {}", ex.getMessage()))
                .flatMap(this::buildEvent)
                .flatMap(event -> {
                    ProducerRecord<String, PostEvent> producerRecord = new ProducerRecord<>("notification_post", event);
                    SenderRecord<String, PostEvent, String> senderRecord = SenderRecord.create(producerRecord, String.valueOf(request.getActionType()));
                    return kafkaSender.send(Mono.just(senderRecord))
                            .doOnError(ex -> log.info("Error while send event: {}", ex.getMessage()))
                            .then(plus(postAction))
                            .then(Mono.just(postAction));
                });
    }

    public Mono<Comment> comment(CommentCreationRequest request) {
        Comment comment = actionMapper.toComment(request);
        comment.setId(String.valueOf(idGenerator.nextId()));
        comment.setActionDate(Instant.now());
        comment.setUpdatedAt(Instant.now());

        PostAction postAction = actionMapper.toPostAction2(comment);
        postAction.setActionType(ActionType.COMMENT);

        return actionRepository.save(postAction)
                .doOnError(ex -> log.info("Error while save action (comment): {}", ex.getMessage()))
                .flatMap(this::buildEvent)
                .flatMap(event -> {
                    ProducerRecord<String, PostEvent> producerRecord = new ProducerRecord<>("notification_post", event);
                    SenderRecord<String, PostEvent, String> senderRecord = SenderRecord.create(producerRecord, String.valueOf(postAction.getActionType()));
                    return kafkaSender.send(Mono.just(senderRecord))
                            .doOnError(ex -> log.info("Error while send event (comment): {}", ex.getMessage()))
                            .then(plus(postAction))
                            .then(commentRepository.save(comment))
                            .doOnError(ex -> log.info("Error while save comment: {}", ex.getMessage()));
                });
    }

    private Mono<PostEvent> buildEvent(PostAction postAction) {
        var postEvent = PostEvent.builder().build();
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
                        .flatMap(profileResponse -> {
                            postEvent.setTitle(profileResponse.getUsername());
                            postEvent.setContent(message(postAction));
                            return getNotificationRecipient(postAction.getPostId(), token)
                                    .collectList()
                                    .flatMap(r1 -> {
                                        r1.addAll(profileResponse.getFriends());
                                        postEvent.setRecipientId(new HashSet<>(r1));
                                        return getAuthorOfPost(postAction.getPostId(), token)
                                                .map(authorName -> {
                                                    postEvent.setContent(postEvent.getContent() + " " + authorName);
                                                    return postEvent;
                                                });
                                    });
                        })
                )
                ;
    }

    private Flux<String> getNotificationRecipient(String postId, String token) {
        return actionRepository.findAllByPostId(postId)
                .map(PostAction::getUserId)
                .flatMap(userId -> webClient.get()
                        .uri("http://localhost:8081/profile/get-by-user-id/{userId}", userId)
                        .header("Authorization","Bearer " + token)
                        .retrieve()
                        .bodyToMono(ProfileResponse.class)
                        .onErrorResume(ex -> {
                            log.info("Exception: {}", ex.getMessage());
                            return Mono.empty();
                        })
                        .map(ProfileResponse::getUsername)
                );
    }

    private String message(PostAction postAction) {
        return switch (postAction.getActionType()) {
            case LIKE -> "vừa thích bài viết của";
            case COMMENT -> "vừa bình luận bài viết của";
            case SHARE -> "vừa chia sẻ bài viết của";
        };
    }

    public Mono<Void> plus(PostAction postAction) {
        return postRepository.findById(postAction.getPostId())
                .switchIfEmpty(Mono.error(new RuntimeException("Post not exists")))
                .flatMap(post -> {
                    log.info("Post before: {}", post);
                    switch (postAction.getActionType()){
                        case COMMENT:
                            post.setCommentCount(post.getCommentCount() + 1);
                            log.info("Post after: {}", post);
                            return postRepository.save(post);
                        case LIKE:
                            post.setLikeCount(post.getLikeCount() + 1);
                            log.info("Post after: {}", post);
                            return postRepository.save(post);
                        case SHARE :
                            post.setShareCount(post.getShareCount() + 1);
                            log.info("Post after: {}", post);
                            return postRepository.save(post);
                        default:
                            log.info("Post after: {}", post);
                            return postRepository.save(post);
                    }
                })
                .then();
    }

    private Mono<String> getAuthorOfPost(String postId, String token){
        return postRepository.findById(postId)
                .flatMap(post -> webClient.get()
                        .uri("http://localhost:8081/profile/get-by-user-id/{userId}", post.getUserId())
                        .header("Authorization","Bearer "+ token)
                        .retrieve()
                        .bodyToMono(ProfileResponse.class)
                        .onErrorResume(ex -> {
                            log.info("Exception: {}", ex.getMessage());
                            return Mono.empty();
                        })
                        .map(ProfileResponse::getUsername)
                );
    }
}
