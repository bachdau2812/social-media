package com.dauducbach.post_service.mapper;

import com.dauducbach.post_service.dto.request.PostCreateRequest;
import com.dauducbach.post_service.dto.response.PostResponse;
import com.dauducbach.post_service.entity.Post;
import com.dauducbach.post_service.entity.PostIndex;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMapper{
    PostResponse toPostResponse1(Post post);
    PostResponse toPostResponse2(PostIndex postIndex);
    Post toPost(PostCreateRequest postCreateRequest);
    PostIndex toPostIndex(Post post);
}