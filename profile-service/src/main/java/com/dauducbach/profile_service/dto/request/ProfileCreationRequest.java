package com.dauducbach.profile_service.dto.request;

import com.dauducbach.profile_service.entity.Profile;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileCreationRequest {
    Long userId;
    String username;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    LocalDate dob;

    String city;
    String job;
    String vehicle;

    List<String> interests;
    List<String> followPage;
}
