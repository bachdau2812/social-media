package com.dauducbach.identity_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class UserCreationRequest {
    String username;
    String password;
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

    Set<String> roles;
}
