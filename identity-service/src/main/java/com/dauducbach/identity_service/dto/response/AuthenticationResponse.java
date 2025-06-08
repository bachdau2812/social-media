package com.dauducbach.identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {

    String token;
}