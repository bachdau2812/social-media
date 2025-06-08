package com.dauducbach.identity_service.mapper;

import com.dauducbach.identity_service.dto.request.PermissionCreationRequest;
import com.dauducbach.identity_service.dto.request.PermissionUpdateRequest;
import com.dauducbach.identity_service.dto.response.PermissionResponse;
import com.dauducbach.identity_service.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionCreationRequest request);
    PermissionResponse toPermissionResponse(Permission permission);

    PermissionResponse updatePermission(PermissionUpdateRequest request);
}
