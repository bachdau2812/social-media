package com.dauducbach.match_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class LocationRequest {
    String userId;
    double latitude;    // Vi do
    double longitude;   // Kinh do
}