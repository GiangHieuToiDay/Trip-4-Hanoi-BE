package com.trip4hanoi.app.dto.req;


import com.trip4hanoi.app.common.PlanType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequest {

    @NotNull(message = "Package type is required")
    private PlanType packageType;

}
