package com.dauducbach.identity_service.service;

import com.dauducbach.event.NotificationEvent;
import com.dauducbach.event.ProfileCreationEvent;
import com.dauducbach.identity_service.dto.request.UserCreationRequest;
import com.dauducbach.identity_service.dto.request.UserUpdateRequest;
import com.dauducbach.identity_service.dto.response.UserResponse;
import com.dauducbach.identity_service.entity.User;
import com.dauducbach.identity_service.entity.UserRole;
import com.dauducbach.identity_service.mapper.UserMapper;
import com.dauducbach.identity_service.repository.RoleRepository;
import com.dauducbach.identity_service.repository.UserRepository;
import com.dauducbach.identity_service.repository.UserRoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    UserRoleRepository userRoleRepository;
    RoleRepository roleRepository;
    IdGenerator idGenerator;
    WebClient webClient;

    R2dbcEntityTemplate r2dbcEntityTemplate;
    PasswordEncoder passwordEncoder;
    KafkaSender<String, NotificationEvent> kafkaSenderNotification;
    KafkaSender<String, ProfileCreationEvent> kafkaSenderProfileEvent;

    @NonFinal
    @Value("${app.services.profile}")
    private String PROFILE_URI;


    public Mono<UserResponse> createUser(UserCreationRequest request) {
        return Mono.zip(
                userRepository.existsByUsername(request.getUsername()),
                isValidRole(request.getRoles())
        )
                .flatMap(tuple -> {
                    boolean t1 = tuple.getT1();
                    boolean t2 = tuple.getT2();

                    if (t1) {
                        return Mono.error(new RuntimeException("User already exists"));
                    }

                    if (!t2) {
                        return Mono.error(new RuntimeException("Invalid role"));
                    }

                    var user = userMapper.toUser(request);
                    user.setId(idGenerator.nextId());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));

                    var profile = userMapper.toProfileCreationEvent(request);
                    profile.setUserId(user.getId());
                    log.info("Profile creation: {}", profile);

                    ProducerRecord<String, ProfileCreationEvent> producerRecord = new ProducerRecord<>("profile-creation", String.valueOf(idGenerator.nextId()), profile);
                    SenderRecord<String, ProfileCreationEvent, String> senderRecord = SenderRecord.create(producerRecord, profile.getUsername());

                    var notificationEvent = NotificationEvent.builder()
                            .chanel("EMAIL")
                            .recipient(List.of(user.getEmail()))
                            .templateCode("CREATE USER COMPLETE")
                            .param(Map.of("bach", "dep trai", "bach dau", "hoc ngu"))
                            .subject("SOCIAL APP")
                            .body("Welcome to our app")
                            .build();

                    ProducerRecord<String, NotificationEvent> producerRecord1 = new ProducerRecord<>("notification_system", String.valueOf(idGenerator.nextId()), notificationEvent);
                    SenderRecord<String, NotificationEvent, String> senderRecord1 = SenderRecord.create(producerRecord1, notificationEvent.getTemplateCode());

                    var userResponse = userMapper.toUserResponse(user);
                    userResponse.setRoles(new HashSet<>());
                    return r2dbcEntityTemplate.insert(User.class).using(user)
                            .flatMap(user1 -> Flux.fromIterable(request.getRoles())
                                    .flatMap(role -> {
                                        UserRole userRole = new UserRole(user.getId(), role);
                                        userResponse.getRoles().add(role);
                                        return r2dbcEntityTemplate.insert(UserRole.class).using(userRole);
                                    })
                                    .then(Mono.just(userResponse))
                            )
                            .then(Mono.when(
                                    kafkaSenderProfileEvent.send(Mono.just(senderRecord))
                                            .doOnError(e -> log.error("Lỗi gửi event profile: {}", e.getMessage()))
                                            .onErrorResume(e -> Mono.empty()),

                                    kafkaSenderNotification.send(Mono.just(senderRecord1))
                                            .doOnError(e -> log.error("Lỗi gửi event notification: {}", e.getMessage()))
                                            .onErrorResume(e -> Mono.empty())
                            ))
                            .thenReturn(userResponse);
                });
    }

    public Mono<Boolean> isValidRole(Set<String> roles) {
        return Flux.fromIterable(roles).flatMap(roleRepository::existsById)
                .all(Boolean::booleanValue);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UserResponse> updateUser(Long id, UserUpdateRequest request) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("User not exists")))
                .flatMap(
                        user -> {
                            UserResponse userResponse = userMapper.toUserResponse(user);
                            return userRoleRepository.findByUserId(userResponse.getId())
                                    .collectList()
                                    .map(HashSet::new)
                                    .map(strings -> {
                                        userResponse.setRoles(strings);
                                        return userResponse;
                                    })
                                    .then(userRepository.save(userMapper.updateUser(request, user)))
                                    .flatMap(user1 -> Flux.fromIterable(request.getRoles())
                                            .flatMap(role -> userRoleRepository.countByUserIdAndRoleName(user.getId(), role)
                                                    .map(count -> count > 0)
                                                    .flatMap(
                                                            roleExisted -> {
                                                                if (roleExisted) {
                                                                    log.info("Role {} already existed for user {}", role, user.getUsername());
                                                                    return Mono.just(true);
                                                                } else {
                                                                    UserRole newEntry = new UserRole(user.getId(), role);
                                                                    return r2dbcEntityTemplate.insert(UserRole.class).using(newEntry);
                                                                }
                                                            }
                                                    )
                                            ).then(Mono.just(userResponse))
                                    );
                        }
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UserResponse> getUserById(Long id){
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("User Not Exists")))
                .map(userMapper::toUserResponse);
    }

    public Mono<UserResponse> getMyInfo(){
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(username -> userRepository.findByUsername(username)
                        .switchIfEmpty(Mono.error(new RuntimeException("User not exists")))
                        .map(userMapper::toUserResponse)
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Flux<UserResponse> getAllUser(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .doOnNext(principal -> {
                    log.info("Principal: {}", principal.getName());
                })
                .thenMany(userRepository.findAll())  // Tiếp tục tìm tất cả người dùng sau khi log
                .map(userMapper::toUserResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deleteUser(Long id, ServerWebExchange exchange) {
        userRoleRepository.deleteByUserId(id).subscribe();
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        webClient.delete()
                .uri(PROFILE_URI + "/delete-by-user-id/{id}",id)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(ex -> {
                    log.error("Loi khi xoa profile: {}", ex.getMessage());
                    return Mono.empty();
                }).subscribe();
        return (userRepository.deleteById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deleteAllUser(ServerWebExchange exchange){
        userRoleRepository.deleteAll().subscribe();
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("Token: {}", token);
        webClient.delete()
                .uri(PROFILE_URI + "/delete-all")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume( ex -> {
                    log.error("Loi khi xoa profile: {}", ex.getMessage());
                    return Mono.empty();
                }).subscribe();
        return userRepository.deleteAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UserResponse> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toUserResponse);
    }

}






























