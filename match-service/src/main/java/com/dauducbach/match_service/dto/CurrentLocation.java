package com.dauducbach.match_service.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CurrentLocation {
    double latitude;    // Vi do
    double longitude;   // Kinh do
}