package com.dauducbach.notification_service.dto.request;

import lombok.*;

import jakarta.validation.constraints.NotNull;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class NotificationRequest {
    @NotNull
    String chanel;      // De luu loai thong bao. vd: email, push, sms, ...
    List<String> recipient;   // Nguoi nhan
    String templateCode;
    Map<String, Object> param;
    @NotNull
    String subject;
    String body;
}
