package com.dauducbach.post_service.controller;

import com.dauducbach.post_service.dto.request.PostCreateRequest;
import com.dauducbach.post_service.dto.request.PostUpdateRequest;
import com.dauducbach.post_service.dto.response.ApiResponse;
import com.dauducbach.post_service.dto.response.PostResponse;
import com.dauducbach.post_service.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController

public class PostController {
    PostService postService;

    @PostMapping
    Mono<ApiResponse<PostResponse>> createPost(@RequestBody PostCreateRequest request) {
        return postService.create(request)
                .map(post -> ApiResponse.<PostResponse>builder()
                        .result(post)
                        .build());
    }

    @PutMapping("/update/{postId}")
    Mono<ApiResponse<PostResponse>> updatePost(@PathVariable String postId,@RequestBody PostUpdateRequest request) {
        return postService.updatePost(postId, request)
                .map(post -> ApiResponse.<PostResponse>builder()
                        .result(post)
                        .build());
    }

    @GetMapping("/get-post/{userId}")
    Flux<ApiResponse<PostResponse>> getPostByUserId(@PathVariable String userId) {
        return postService.getAllPostByUserId(userId)
                .map(post -> ApiResponse.<PostResponse>builder()
                        .result(post)
                        .build());
    }

    @DeleteMapping("/delete-post/{userId}")
    Mono<ApiResponse<Void>> deletePostByUserId(@PathVariable String userId) {
        return postService.deleteAllPostByUserId(userId)
                .map(post -> ApiResponse.<Void>builder().build());
    }

    @DeleteMapping("/{postId}")
    Mono<ApiResponse<Void>> deletePost(@PathVariable String postId) {
        return postService.deletePost(postId)
                .map(post -> ApiResponse.<Void>builder().build());
    }

    @GetMapping("/{postId}")
    Mono<ApiResponse<PostResponse>> getPost(@PathVariable String postId) {
        return postService.getPost(postId)
                .map(post -> ApiResponse.<PostResponse>builder().result(post).build());
    }

    @DeleteMapping("/delete-all")
    Mono<Void> deleteAll() {
        return postService.deleteAll();
    }
}