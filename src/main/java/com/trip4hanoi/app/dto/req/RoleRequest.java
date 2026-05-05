package com.trip4hanoi.app.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleRequest {
    private Long id;

    @NotBlank(message = "NAME_REQUIRED")
    private String name;

    private String description;
    private Set<Long> permissions; // Đổi từ Set<String> sang Set<Long>
}
