package com.dauducbach.chat_service.service;

import com.dauducbach.chat_service.dto.request.ApprovalRequest;
import com.dauducbach.chat_service.dto.request.GroupCreationRequest;
import com.dauducbach.chat_service.dto.request.GroupUpdateRequest;
import com.dauducbach.chat_service.dto.request.JoinGroupRequest;
import com.dauducbach.chat_service.entity.Group;
import com.dauducbach.chat_service.mapper.GroupMapper;
import com.dauducbach.chat_service.repository.GroupRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class GroupService {
    GroupRepository groupRepository;
    GroupMapper groupMapper;

    public Mono<Group> createGroup(GroupCreationRequest request) {
        Group group = groupMapper.toGroup(request);
        group.setId(UUID.randomUUID().toString());
        group.setCreateAt(Instant.now());

        group.setMemberIds(new HashSet<>());
        group.getMemberIds().add(group.getCreateBy());

        group.setAdminIds(new HashSet<>());
        group.getAdminIds().add(group.getCreateBy());

        return groupRepository.save(group);
    }

    public Mono<Group> updateGroup(String groupId, GroupUpdateRequest request) {
        return groupRepository.findById(groupId)
                .switchIfEmpty(Mono.error(new RuntimeException("Group not exists")))
                .flatMap(group -> {
                    if(request.getName() != null) group.setName(request.getName());
                    if(request.getDescription() != null) group.setDescription(request.getDescription());
                    if(request.getAvatarUrl() != null) group.setAvatarUrl(request.getAvatarUrl());
                    if(request.isPublic() != group.isPublic()) group.setPublic(request.isPublic());
                    return groupRepository.save(group);
                });
    }

    public Mono<Void> joinGroup(JoinGroupRequest request) {
        return groupRepository.findById(request.getGroupId())
                .switchIfEmpty(Mono.error(new RuntimeException("Group not exists")))
                .flatMap(group -> {
                    if(group.isRequireApproval()) {
                        group.getPendingJoinRequest().add(request.getUserId());
                        return groupRepository.save(group);
                    }else {
                        group.getMemberIds().add(request.getUserId());
                        return groupRepository.save(group);
                    }
                })
                .then();
    }

    public Mono<Void> approvalUserJoinGroup(ApprovalRequest request) {
        return groupRepository.findById(request.getGroupId())
                .flatMap(group -> {
                    group.getMemberIds().add(request.getUserId());
                    group.getPendingJoinRequest().remove(request.getUserId());
                    return groupRepository.save(group);
                })
                .then();
    }

    public Mono<Group> getGroupById(String groupId) {
        return groupRepository.findById(groupId);
    }

    public Mono<Void> deleteGroup(String groupId){
        return groupRepository.deleteById(groupId);
    }

}