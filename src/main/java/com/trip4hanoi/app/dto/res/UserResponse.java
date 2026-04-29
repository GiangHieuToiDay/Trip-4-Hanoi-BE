package com.trip4hanoi.app.dto.res;

import com.trip4hanoi.app.entity.Role;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String googleId;
    private String nationality;
    private String language;
    private LocalDateTime createdAt;
}
