package com.trip4hanoi.app.controller;


import com.trip4hanoi.app.dto.req.RoleRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.RoleResponse;
import com.trip4hanoi.app.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
@Slf4j(topic = "ROLE CONTROLLER")
@Tag(name = "Role Management", description = "APIs for managing user roles and permissions")
@PreAuthorize("hasAuthority('MANAGE_ROLE')")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Create role", description = "Create a new user role")
    @PostMapping
    public ResponseEntity<APIResponse<RoleResponse>> create(@Valid @RequestBody RoleRequest request) {
        RoleResponse role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.<RoleResponse>builder()
                .status(HttpStatus.CREATED.value())
                .code(1000)
                .message("Role created successfully")
                .data(role)
                .build());
    }

    @Operation(summary = "Get list roles", description = "Get list of all system roles with pagination")
    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<RoleResponse>>> getAll(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "id:asc") String sort,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        PageResponse<RoleResponse> result = roleService.getAllRoles(keyword, sort, page, size);

        return ResponseEntity.ok(APIResponse.<PageResponse<RoleResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Get role list successfully")
                .data(result)
                .build());
    }

    @Operation(summary = "Get all roles list", description = "Get all roles without pagination for dropdowns")
    @GetMapping("/all")
    public ResponseEntity<APIResponse<List<RoleResponse>>> getAllList() {
        return ResponseEntity.ok(APIResponse.<List<RoleResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Get all roles list successfully")
                .data(roleService.getAllList())
                .build());
    }

    @Operation(summary = "Update role", description = "Update role details and permissions")
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<RoleResponse>> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        RoleResponse role = roleService.updateRole(id, request);
        return ResponseEntity.ok(APIResponse.<RoleResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Role updated successfully")
                .data(role)
                .build());
    }

    @Operation(summary = "Delete role", description = "Delete a system role")
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Role deleted successfully")
                .build());
    }
}
