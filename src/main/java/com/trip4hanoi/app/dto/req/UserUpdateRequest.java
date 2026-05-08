package com.trip4hanoi.app.dto.req;


import com.trip4hanoi.app.common.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    private Long id;


    @NotBlank(message = "INVALID_EMAIL")
    @Email(message = "INVALID_EMAIL")
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String avatar;
    private UserStatus status;
    private Set<Long> roles;
    private Boolean isLocationTrackingEnabled;
}
