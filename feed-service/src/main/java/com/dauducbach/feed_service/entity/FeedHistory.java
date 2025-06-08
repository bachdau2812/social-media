package com.dauducbach.feed_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

@Document("feed_history")
public class FeedHistory {
    @Id
    String id;
    String postId;
    String username;
    double timestamp;
}
