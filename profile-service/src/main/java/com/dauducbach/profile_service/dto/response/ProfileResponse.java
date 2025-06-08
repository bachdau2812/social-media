package com.dauducbach.profile_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ProfileResponse {
    Long id;
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

    List<String> friends;
    List<String> followUser;
    List<String> restrictions;
    List<String> blocks;
    List<String> invites;
    List<String> interests;
    List<String> followPage;
}
