package com.dauducbach.chat_service.mapper;

import com.dauducbach.chat_service.dto.request.MessageRequest;
import com.dauducbach.chat_service.entity.Message;
import com.dauducbach.event.MessageEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    Message toMessage(MessageRequest request);
    MessageEvent toMessageEvent(Message message);
}
