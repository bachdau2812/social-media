package com.dauducbach.chat_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class GroupCreationRequest {
    String name;
    String description;
    String avatarUrl;

    String createBy;
    boolean requireApproval;
    boolean isPublic;
}