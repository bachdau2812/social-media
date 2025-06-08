package com.dauducbach.notification_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class UserResponse {
    Long id;
    String username;
    String password;
    String email;
    String phoneNumber;
    LocalDate dob;

    Set<String> roles;
}
