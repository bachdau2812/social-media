package com.dauducbach.chat_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

@Document("groups")
public class Group {
    String id;
    String name;
    String description;
    String avatarUrl;

    String createBy;
    Instant createAt;

    Set<String> memberIds;
    Set<String> adminIds;
    boolean requireApproval;

    Set<String> pendingJoinRequest;
    boolean isPublic;
}