package com.dauducbach.identity_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

@Table("users")
public class User {
    @Id
    Long id;
    String username;
    String password;
    String email;
    String phoneNumber;
    LocalDate dob;
}
