package com.dauducbach.post_service.mapper;

import com.dauducbach.post_service.dto.request.ActionRequest;
import com.dauducbach.post_service.dto.request.CommentCreationRequest;
import com.dauducbach.post_service.entity.Comment;
import com.dauducbach.post_service.entity.PostAction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActionMapper {
    Comment toComment(CommentCreationRequest request);
    PostAction toPostAction(ActionRequest request);

    PostAction toPostAction2(Comment comment);
}