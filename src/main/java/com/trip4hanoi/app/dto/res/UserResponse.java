package com.trip4hanoi.app.dto.res;

import com.trip4hanoi.app.common.AuthProvider;
import com.trip4hanoi.app.common.UserStatus;
import com.trip4hanoi.app.entity.Role;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private Set<RoleResponse> roles;
    private String nationality;
    private String language;
    private String providerId;
    private AuthProvider provider;
    private String verificationCode;
    private UserStatus status;
    private LocalDateTime createdAt;
}
