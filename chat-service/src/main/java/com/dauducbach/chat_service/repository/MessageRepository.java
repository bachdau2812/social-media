package com.dauducbach.chat_service.repository;

import com.dauducbach.chat_service.entity.Message;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageRepository extends ReactiveMongoRepository<Message, String> {
    Flux<Message> findAllByMessageFrom(String messageFrom);
    Flux<Message> findAllByMessageFromAndMessageTo(String messageFrom, String messageTo);
}
