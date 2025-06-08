package com.dauducbach.chat_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SearchMessageRequest {
    String messageFrom;
    String messageTo;
    String query;
}