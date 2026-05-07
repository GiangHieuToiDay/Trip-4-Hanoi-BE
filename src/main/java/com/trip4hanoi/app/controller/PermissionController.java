package com.trip4hanoi.app.controller;


import com.trip4hanoi.app.dto.req.PermissionRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PermissionResponse;
import com.trip4hanoi.app.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/permissions")
@Slf4j(topic = "PERMISSION CONTROLLER")
@Tag(name = "Permission Management", description = "APIs for managing granular permissions")
@PreAuthorize("hasAuthority('MANAGE_ROLE')")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "Create permission", description = "Create a new granular permission")
    @PostMapping
    public ResponseEntity<APIResponse<PermissionResponse>> create(@Valid @RequestBody PermissionRequest request) {
        PermissionResponse permission = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.<PermissionResponse>builder()
                .status(HttpStatus.CREATED.value())
                .code(1000)
                .message("Permission created successfully")
                .data(permission)
                .build());
    }

    @Operation(summary = "Get list permissions", description = "Get list of all granular permissions with pagination")
    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<PermissionResponse>>> getAll(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "id:asc") String sort,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        PageResponse<PermissionResponse> result = permissionService.getAllPermissions(keyword, sort, page, size);

        return ResponseEntity.ok(APIResponse.<PageResponse<PermissionResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Get permission list successfully")
                .data(result)
                .build());
    }

    @Operation(summary = "Update permission", description = "Update permission details")
    @PutMapping
    public ResponseEntity<APIResponse<PermissionResponse>> update(@Valid @RequestBody PermissionRequest request) {
        PermissionResponse permission = permissionService.updatePermission(request.getId(), request);
        return ResponseEntity.ok(APIResponse.<PermissionResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Permission updated successfully")
                .data(permission)
                .build());
    }

    @Operation(summary = "Delete permission", description = "Delete a granular permission by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> delete(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Permission deleted successfully")
                .build());
    }
}
