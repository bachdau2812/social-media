package com.dauducbach.notification_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

@Document("notification")
public class NotificationDB {
    @Id
    Long id;
    LocalDate timestamp;
    List<String> deviceToken;
    String title;
    String body;
}
