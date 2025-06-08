package com.dauducbach.identity_service.service;

import com.dauducbach.identity_service.dto.request.AuthenticationRequest;
import com.dauducbach.identity_service.dto.request.IntrospectRequest;
import com.dauducbach.identity_service.dto.request.LogoutRequest;
import com.dauducbach.identity_service.dto.request.RefreshRequest;
import com.dauducbach.identity_service.dto.response.AuthenticationResponse;
import com.dauducbach.identity_service.dto.response.IntrospectResponse;
import com.dauducbach.identity_service.entity.InvalidatedToken;
import com.dauducbach.identity_service.entity.User;
import com.dauducbach.identity_service.repository.InvalidatedRepository;
import com.dauducbach.identity_service.repository.RolePermissionRepository;
import com.dauducbach.identity_service.repository.UserRepository;
import com.dauducbach.identity_service.repository.UserRoleRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {
    UserRepository userRepository;
    UserRoleRepository userRoleRepository;
    RolePermissionRepository rolePermissionRepository;
    IdGenerator idGenerator;
    InvalidatedRepository invalidatedRepository;
    PasswordEncoder passwordEncoder;

    R2dbcEntityTemplate r2dbcEntityTemplate;
    ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @NonFinal
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    private Long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    private Long REFRESHABLE_DURATION;


    public Mono<String> generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        return buildScope(user)
                .flatMap(scope -> {
                    JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                            .subject(user.getId().toString())
                            .issuer("com.bachdauduc")
                            .issueTime(new Date())
                            .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                            .jwtID(String.valueOf(idGenerator.nextId()))
                            .claim("scope", scope)
                            .build();

                    Payload payload = new Payload(jwtClaimsSet.toJSONObject());

                    JWSObject jwsObject = new JWSObject(jwsHeader, payload);

                    try {
                        jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
                        return Mono.just(jwsObject.serialize());
                    } catch (JOSEException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public Mono<String> buildScope (User user) {
        return userRoleRepository.findByUserId(user.getId())
                .switchIfEmpty(Mono.fromRunnable(() -> System.out.println("Not have role !")))
                .collectList()
                .flatMap(listRole -> Flux.fromIterable(listRole)
                        .flatMap(roleName -> rolePermissionRepository.findByRoleName(roleName)
                                .collectList()
                                .map(permissions -> {
                                    StringJoiner stringJoiner = new StringJoiner(" ");
                                    stringJoiner.add("ROLE_" + roleName);
                                    permissions.forEach(permission -> stringJoiner.add("PERMISSION_" + permission));
                                    return stringJoiner.toString();
                                }
                                )
                        )
                        .collectList()
                        .map(scopes -> String.join(" ", scopes))
                );
    }

    public Mono<SignedJWT> verifyToken (String token, boolean isFresh) {
        return Mono.defer(() -> {
            try {
                JWSVerifier jwsVerifier = new MACVerifier(SIGNER_KEY.getBytes());
                SignedJWT signedJWT = SignedJWT.parse(token);

                boolean verifier = signedJWT.verify(jwsVerifier);
                Date expiryTime = (isFresh)
                        ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                        : signedJWT.getJWTClaimsSet().getExpirationTime();

                if (!(verifier && expiryTime.after(new Date()))) {
                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));
                }

                return invalidatedRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())
                        .flatMap(
                                exists -> exists
                                ? Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"))
                                : Mono.just(signedJWT)
                        );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Mono<IntrospectResponse> introspect(IntrospectRequest request) {
        return verifyToken(request.getToken(), false)
                .thenReturn(new IntrospectResponse(true))
                .onErrorReturn(new IntrospectResponse(false));
    }

    public Mono<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        log.info("Request: {}", request);
        return userRepository.findByUsername(request.getUsername())
                .switchIfEmpty(Mono.error(new RuntimeException("User not exists")))
                .flatMap(user -> {
                   boolean isValid = passwordEncoder.matches(request.getPassword(), user.getPassword());

                   if (isValid) {
                       return generateToken(user)
                               .flatMap(token -> reactiveRedisTemplate.opsForValue().set(user.getId().toString(), token)
                                       .thenReturn(new AuthenticationResponse(token)));
                   } else {
                       return Mono.error(new RuntimeException("Invalid password"));
                   }
                });
    }

    public Mono<AuthenticationResponse> refreshToken(RefreshRequest request) {
        return verifyToken(request.getToken(), true)
                .flatMap(signedJWT -> {
                    try {
                        String userId = signedJWT.getJWTClaimsSet().getSubject();
                        return userRepository.findById(Long.parseLong(userId))
                                .flatMap(this::generateToken)
                                .flatMap(token -> reactiveRedisTemplate.opsForValue().set(userId, token)
                                        .thenReturn(new AuthenticationResponse(token)));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public Mono<Void> logout(LogoutRequest request) {
        return verifyToken(request.getToken(), true)
                .flatMap(signedJWT -> {
                    try {
                        String jit = signedJWT.getJWTClaimsSet().getJWTID();
                        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
                        r2dbcEntityTemplate.insert(InvalidatedToken.class).using(new InvalidatedToken(jit, expiryTime.toInstant())).subscribe();
                        reactiveRedisTemplate.delete(signedJWT.getJWTClaimsSet().getSubject())
                                .doOnSuccess(System.out::println)
                                .subscribe();
                        return Mono.empty();
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}




























