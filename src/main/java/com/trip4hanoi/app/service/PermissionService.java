package com.trip4hanoi.app.service;


import com.trip4hanoi.app.dto.req.PermissionRequest;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PermissionResponse;

public interface PermissionService {
    PermissionResponse createPermission(PermissionRequest request);
    PageResponse<PermissionResponse> getAllPermissions(String keyword, String sort, int page, int size);
    PermissionResponse updatePermission(Long id, PermissionRequest request);
    void deletePermission(Long id);
}
