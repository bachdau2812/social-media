package com.dauducbach.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class MessageEvent {
    String messageId;
    String channel;
    String messageFrom;
    String messageTo;
    String content;
    LocalDateTime createAt;
    LocalDateTime lastModified;
}
