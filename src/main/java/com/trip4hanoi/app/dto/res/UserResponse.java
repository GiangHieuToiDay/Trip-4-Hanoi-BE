package com.trip4hanoi.app.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.trip4hanoi.app.common.AuthProvider;
import com.trip4hanoi.app.common.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(nullable = true)
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Builder.Default
    private String avatar = "";

    private Set<RoleResponse> roles;

    @Schema(nullable = true)
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Builder.Default
    private String nationality = "Vietnam";

    @Schema(nullable = true)
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Builder.Default
    private String language = "vi";

    @Schema(nullable = true)
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Builder.Default
    private String providerId = "";

    private AuthProvider provider;

    @Schema(nullable = true)
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Builder.Default
    private String verificationCode = "";

    private UserStatus status;

    // Chuẩn hóa format ngày tháng cho Swagger/Schemathesis (Thêm offset múi giờ)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;
}