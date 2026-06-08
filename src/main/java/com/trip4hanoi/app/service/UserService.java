package com.trip4hanoi.app.service;


import com.trip4hanoi.app.dto.req.ChangePasswordRequest;
import com.trip4hanoi.app.dto.req.ForgotPasswordRequest;
import com.trip4hanoi.app.dto.req.ResetPasswordRequest;
import com.trip4hanoi.app.dto.req.UserCreateRequest;
import com.trip4hanoi.app.dto.req.UserUpdateRequest;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {

    UserResponse createUser(UserCreateRequest request, MultipartFile file) throws IOException;

    PageResponse<UserResponse> getAllUsers(String keyword, String sort, int page, int size);

    UserResponse getUserById(Long id);

    void updateUser(UserUpdateRequest request, MultipartFile file) throws IOException;

    void deleteUser(Long id);
    UserResponse getMyInfo();
    void changeMyPassword(ChangePasswordRequest request);

    void sendForgotPasswordEmail(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
