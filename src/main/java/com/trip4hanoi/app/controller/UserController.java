package com.trip4hanoi.app.controller;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip4hanoi.app.dto.req.ChangePasswordRequest;
import com.trip4hanoi.app.dto.req.UserCreateRequest;
import com.trip4hanoi.app.dto.req.UserUpdateRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.UserResponse;
import com.trip4hanoi.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j(topic = "USER CONTROLLER")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper;



    @Operation(summary = "Create user" , description = "Api create user to  database")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<UserResponse>> createUser(
            @RequestPart("data") @Valid UserCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        
        // upload avatar if present


        UserResponse userResponse = userService.createUser(request, file);

        APIResponse<UserResponse> response =  APIResponse.<UserResponse>builder()
                .status(HttpStatus.CREATED.value())
                .code(1000)
                .message("Successfully created user")
                .data(userResponse)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Operation(summary = "Get list user", description = "Api get list user from database")
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_USER')")
    public ResponseEntity<APIResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(required = false , defaultValue = "") String keyword,
            @RequestParam(required = false) String  sort,
            @RequestParam(required = false , defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ){

        PageResponse<UserResponse> result = userService.getAllUsers(keyword, sort, page, size);

        APIResponse<PageResponse<UserResponse>> response = APIResponse.<PageResponse<UserResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Get user list successfully")
                .data(result)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @Operation(summary = "Update User", description ="Api update user to database")
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('MANAGE_USER') or (authentication.principal != null)")
    public ResponseEntity<APIResponse<Void>> updateUser(
            @RequestPart("data") String requestJson,
            @RequestPart(value = "file" , required = false) MultipartFile file) throws IOException {

        log.info("Received update request JSON: {}", requestJson);
        UserUpdateRequest request = objectMapper.readValue(requestJson, UserUpdateRequest.class);

        userService.updateUser(request, file);

         APIResponse<Void> response = APIResponse.<Void>builder()
                 .status(HttpStatus.ACCEPTED.value())
                 .code(1000)
                 .message("Successfully updated user")
                 .build();

         return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }


    @Operation(summary = "Inactive account", description = "Api inactive account to database")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USER') or (authentication.principal != null and authentication.principal.claims['id'] == #id)")
    public ResponseEntity<APIResponse<Void>> inactiveAccount(@PathVariable Long id){

        userService.deleteUser(id);

        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully updated user")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary ="Get my info", description = "Api get my info by Id")
    @GetMapping("/me")
    public ResponseEntity<APIResponse<UserResponse>>  getMyInfo() {

        UserResponse userResponse = userService.getMyInfo();

        APIResponse response = APIResponse.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully get my info")
                .data(userResponse)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Change my password" , description = "Api change my password to database")
    @PutMapping("/change-password")
    public ResponseEntity<APIResponse<Void>> changePasswordUser(@Valid @RequestBody ChangePasswordRequest  request) {

        userService.changeMyPassword(request);

        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Changed My Password Successfully")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
