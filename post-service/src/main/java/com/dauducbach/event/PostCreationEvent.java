package com.dauducbach.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostCreationEvent {
    String authorName;
    String postId;
    Instant timestamp;
    Set<String> recipientId;
}
