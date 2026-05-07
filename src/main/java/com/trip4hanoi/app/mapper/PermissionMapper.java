package com.trip4hanoi.app.mapper;


import com.trip4hanoi.app.dto.req.PermissionRequest;
import com.trip4hanoi.app.dto.res.PermissionResponse;
import com.trip4hanoi.app.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResponse toPermissionResponse(Permission entity);
    Permission  toPermissionEntity(PermissionRequest request);
}
