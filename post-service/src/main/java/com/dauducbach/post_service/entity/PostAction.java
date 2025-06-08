package com.dauducbach.post_service.entity;

import com.dauducbach.post_service.constant.ActionType;
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

@Document("actions")
public class PostAction {
    @Id
    String id;
    String userId;
    String postId;
    ActionType actionType;
    Instant actionDate;
}