package com.dauducbach.chat_service.mapper;

import com.dauducbach.chat_service.dto.request.GroupCreationRequest;
import com.dauducbach.chat_service.entity.Group;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    Group toGroup(GroupCreationRequest request);
}