package com.trip4hanoi.app.service;



import com.trip4hanoi.app.dto.req.RoleRequest;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.RoleResponse;

import java.util.List;

public interface RoleService {
    RoleResponse createRole(RoleRequest request);
    PageResponse<RoleResponse> getAllRoles(String keyword, String sort, int page, int size);
    RoleResponse updateRole(Long id, RoleRequest request);
    void deleteRole(Long id);
    List<RoleResponse> getAllList();
}
