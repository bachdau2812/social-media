package com.dauducbach.notification_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class EmailResponse {
    String messageId;       // ID do hệ thống gửi email (Brevo, Mailgun, AWS SES, v.v.)
    String status;          // SUCCESS hoặc FAILED
    Instant sentAt;         // Thời điểm gửi
    String errorMessage;    // Nếu lỗi thì lưu thông tin lỗi
}