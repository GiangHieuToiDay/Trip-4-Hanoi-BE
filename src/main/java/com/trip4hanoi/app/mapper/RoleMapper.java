package com.trip4hanoi.app.mapper;



import com.trip4hanoi.app.dto.req.RoleRequest;
import com.trip4hanoi.app.dto.res.RoleResponse;
import com.trip4hanoi.app.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {
    RoleResponse toRoleResponse(Role entity);

    @Mapping(target = "permissions", ignore = true)
    Role toRoleEntity(RoleRequest request);
}
