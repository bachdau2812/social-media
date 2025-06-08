package com.dauducbach.profile_service.mapper;

import com.dauducbach.event.ProfileCreationEvent;
import com.dauducbach.profile_service.dto.request.ProfileCreationRequest;
import com.dauducbach.profile_service.dto.request.ProfileUpdateRequest;
import com.dauducbach.profile_service.dto.response.ProfileResponse;
import com.dauducbach.profile_service.entity.Profile;
import com.dauducbach.profile_service.entity.ProfileIndex;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileCreationRequest toProfileCreationRequest(ProfileCreationEvent event);

    Profile toProfile(ProfileCreationRequest request);

    @Mapping(target = "friends", expression = "java(toUsernames(profile.getFriends()))")
    @Mapping(target = "followUser", expression = "java(toUsernames(profile.getFollowUser()))")
    @Mapping(target = "restrictions", expression = "java(toUsernames(profile.getRestrictions()))")
    @Mapping(target = "blocks", expression = "java(toUsernames(profile.getBlocks()))")
    @Mapping(target = "invites", expression = "java(toUsernames(profile.getInvites()))")
    ProfileResponse toProfileResponse(Profile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Profile updateProfile(ProfileUpdateRequest request, @MappingTarget Profile profile);

    default List<String> toUsernames(List<Profile> profiles) {
        if (profiles == null) return List.of();
        return profiles.stream()
                .map(Profile::getUsername)
                .collect(Collectors.toList());
    }

    ProfileIndex toProfileIndex(Profile profile);
}
