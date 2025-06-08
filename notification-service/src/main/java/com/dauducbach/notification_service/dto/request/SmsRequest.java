package com.dauducbach.notification_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SmsRequest {
    String sender;
    String recipient;
    String subject;
    String content;
    String type;
}
