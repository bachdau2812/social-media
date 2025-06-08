package com.dauducbach.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationEvent {
    String chanel;      // De luu loai thong bao. vd: email, push, sms, ...
    List<String> recipient;   // Nguoi nhan
    String templateCode;
    Map<String, Object> param;
    String subject;
    String body;
}
