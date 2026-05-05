package com.trip4hanoi.app.dto.req;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionRequest {
    private Long id;

    @NotBlank(message = "NAME_REQUIRED")
    private String name;

    private String description;
}
