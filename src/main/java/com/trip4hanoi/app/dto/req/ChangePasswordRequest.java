package com.trip4hanoi.app.dto.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    @NotEmpty(message = "Old password must not be empty")
    private String oldPassword;

    @NotEmpty(message = "New password must not be empty")
    @Size(min = 6, message = "New password must be at least 6 characters long")
    private String newPassword;

}
