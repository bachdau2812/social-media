package com.dauducbach.chat_service.service;

import com.dauducbach.chat_service.dto.request.SearchMessageRequest;
import com.dauducbach.chat_service.entity.Message;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class SearchMessageService {
    ReactiveMongoTemplate reactiveMongoTemplate;

    public Flux<Message> searchMessage(SearchMessageRequest request) {
        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.add(Criteria.where("messageFrom").is(request.getMessageFrom()));
        criteriaList.add(Criteria.where("messageTo").is(request.getMessageTo()));

        criteriaList.add(Criteria.where("content").regex(".*" + Pattern.quote(request.getQuery()) + ".*", "i"));

        Criteria combinedCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        Query query = new Query(combinedCriteria)
                .with(Sort.by(Sort.Direction.DESC, "createAt"));

        return reactiveMongoTemplate.find(query, Message.class);
    }
}
