package com.dauducbach.post_service.controller;

import com.dauducbach.post_service.dto.request.ActionRequest;
import com.dauducbach.post_service.dto.request.CommentCreationRequest;
import com.dauducbach.post_service.entity.Comment;
import com.dauducbach.post_service.entity.PostAction;
import com.dauducbach.post_service.service.PostActionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController

@RequestMapping("/actions")
public class ActionController {
    PostActionService postActionService;

    @PostMapping("/like-share")
    Mono<PostAction> likeAndShare(@RequestBody ActionRequest request) {
        return postActionService.like_and_share(request);
    }

    @PostMapping("/comment")
    Mono<Comment> comment(@RequestBody CommentCreationRequest request) {
        return postActionService.comment(request);
    }


}
