package com.dauducbach.post_service.dto.request;

import com.dauducbach.post_service.constant.ActionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActionRequest {
    String userId;
    String postId;
    ActionType actionType;
}