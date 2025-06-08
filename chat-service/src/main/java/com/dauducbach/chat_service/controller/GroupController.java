package com.dauducbach.chat_service.controller;

import com.dauducbach.chat_service.dto.request.GroupCreationRequest;
import com.dauducbach.chat_service.dto.request.GroupUpdateRequest;
import com.dauducbach.chat_service.entity.Group;
import com.dauducbach.chat_service.service.GroupService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

@RequestMapping("/groups")
public class GroupController {
    GroupService groupService;

    @PostMapping
    public Mono<Group> create(@RequestBody GroupCreationRequest request) {
        return groupService.createGroup(request);
    }

    @PutMapping("/{groupId}")
    public Mono<Group> update(@PathVariable String groupId, @RequestBody GroupUpdateRequest request) {
        return groupService.updateGroup(groupId, request);
    }

    @GetMapping("/{groupId}")
    public Mono<Group> getGroup(@PathVariable String groupId) {
        return groupService.getGroupById(groupId);
    }

    @DeleteMapping("/{groupId}")
    public Mono<Void> delete(@PathVariable String groupId) {
        return groupService.deleteGroup(groupId);
    }

}
