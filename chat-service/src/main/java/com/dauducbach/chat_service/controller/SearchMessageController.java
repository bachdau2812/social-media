package com.dauducbach.chat_service.controller;

import com.dauducbach.chat_service.dto.request.SearchMessageRequest;
import com.dauducbach.chat_service.entity.Message;
import com.dauducbach.chat_service.service.SearchMessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

@RequestMapping("/search")
public class SearchMessageController {
    SearchMessageService searchMessageService;

    @PostMapping
    public Flux<Message> searchMessage(@RequestBody SearchMessageRequest request) {
        return searchMessageService.searchMessage(request);
    }
}
