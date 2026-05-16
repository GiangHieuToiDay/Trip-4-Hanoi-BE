package com.trip4hanoi.app.dto.req;

import com.trip4hanoi.app.common.UserStatus;
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
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String password;
    private String avatar;
    private String nationality;
    private String language;
    private UserStatus status;
    private Set<Long> roles;
    private Boolean isLocationTrackingEnabled;
}
