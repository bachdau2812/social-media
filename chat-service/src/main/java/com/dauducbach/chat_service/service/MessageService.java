package com.dauducbach.chat_service.service;

import com.dauducbach.chat_service.dto.request.MessageEditRequest;
import com.dauducbach.chat_service.dto.request.MessageRequest;
import com.dauducbach.chat_service.dto.response.ApiResponse;
import com.dauducbach.chat_service.dto.response.UserResponse;
import com.dauducbach.chat_service.entity.Message;
import com.dauducbach.chat_service.mapper.MessageMapper;
import com.dauducbach.chat_service.repository.MessageRepository;
import com.dauducbach.event.MessageEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class MessageService {
    WebClient webClient;
    MessageRepository messageRepository;
    MessageMapper messageMapper;
    IdGenerator idGenerator;
    KafkaSender<String, MessageEvent> kafkaSender;

    public Mono<Message> saveMessage(MessageRequest request){
        Message message = messageMapper.toMessage(request);

        message.setMessageId(String.valueOf(idGenerator.nextId()));
        message.setCreateAt(LocalDateTime.now());
        message.setLastModified(LocalDateTime.now());

        return messageRepository.save(message)
                .flatMap(message1 -> getUserName(request)
                        .flatMap(username -> {
                            // Nếu ở phần chat nhóm thì phải tìm hết thành viên của nhóm đó, rồi thêm và messageTo và gửi push cho tất cả các thành viên trong nhóm.

                            var messageEvent = messageMapper.toMessageEvent(message1);
                            messageEvent.setMessageFrom(username);
                            ProducerRecord<String, MessageEvent> producerRecord = new ProducerRecord<>("notification_chat",  message1.getMessageFrom(), messageEvent);
                            SenderRecord<String, MessageEvent, String> senderRecord = SenderRecord.create(producerRecord, message1.getMessageTo());

                            return kafkaSender.send(Mono.just(senderRecord))
                                    .doOnError(ex -> log.info("Error: {}", ex.getMessage()))
                                    .then(Mono.just(message));
                        }));
    }

    public Mono<String> getUserName(MessageRequest messageRequest) {
        return ReactiveSecurityContextHolder.getContext()
                .doOnSuccess(context -> log.info("{}", context))
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    Jwt jwt = (Jwt) principal;
                    log.info("Jwt: {}", jwt);
                    return "Bearer " + jwt.getTokenValue();
                })
                .doOnSuccess(token -> log.info("Token: {}","Bearer " +  token))
                .doOnError(ex -> log.info("Error while get token: {}", ex.getMessage()))
                .flatMap(token -> webClient.get()
                        .uri("http://localhost:8080/identity/users/get-user-id/{id}", messageRequest.getMessageFrom())
                        .header("Authorization", token)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserResponse>>() {})
                        .onErrorResume(ex -> Mono.error(new RuntimeException("Error while get user id: " + ex.getMessage())))
                        .map(ApiResponse::getResult)
                )
                .map(UserResponse::getUsername);
    }

    public Mono<Message> editMessage(String messageId, MessageEditRequest request) {
        return messageRepository.findById(messageId)
                .switchIfEmpty(Mono.error(new RuntimeException("Message not exists")))
                .flatMap(message -> {
                    message.setContent(request.getContent());
                    message.setLastModified(LocalDateTime.now());
                    return messageRepository.save(message);
                });
    }

    public Flux<Message> getAllBySenderIdAndReceiverId(String messageFrom, String messageTo) {
        return messageRepository.findAllByMessageFromAndMessageTo(messageFrom, messageTo)
                .switchIfEmpty(Mono.error(new RuntimeException("Chat history is empty!")));
    }

    public Mono<String> deleteMessage(String messageId) {
        return messageRepository.existsById(messageId)
                .flatMap(isExists -> {
                    if (isExists) {
                        return messageRepository.deleteById(messageId)
                                .then(Mono.just("Delete complete !"));
                    } else {
                        return Mono.just("Message not exists");
                    }
                });
    }
}
