package com.dauducbach.post_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

@Document("comments")
public class Comment {
    @Id
    String id;
    String postId;
    String userId;
    String content;
    Instant actionDate;
    Instant updatedAt;
    String parentCommentId;
}