package com.dauducbach.match_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

@Document("location_history")
public class LocationHistory {
    @Id
    String id;
    String userId;

    double latitude;    // Vi do
    double longitude;   // Kinh do

    Instant timestamp;
    String deviceName;

    String geoLocation;
}