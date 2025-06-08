package com.dauducbach.chat_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

@Document("message")
public class Message {
    @Id
    String messageId;
    String channel;
    String messageFrom;
    String messageTo;
    String content;
    LocalDateTime createAt;
    LocalDateTime lastModified;
}
