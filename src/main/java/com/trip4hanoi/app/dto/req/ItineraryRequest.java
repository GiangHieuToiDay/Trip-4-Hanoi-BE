package com.trip4hanoi.app.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CrossOrigin(origins = "http://localhost:5173")
public class ItineraryRequest {

    private Long id;

    @NotBlank(message = "Title can not blank")
    private String title;

    @NotNull(message = "Budget can not nulll")
    @Min(value = 1000, message = "Budget must >= 1000")
    private Integer budget;

    @NotNull(message = "Days can not null")
    @Min(value = 1, message = "Days must >= 1")
    private Integer days;

    @NotNull(message = "Number of people can not null")
    @Min(value = 1, message = "There must be at least 1 people.")
    private Integer numberOfPeople;

    private List<@NotBlank(message = "Category name invalid") String> categoryNames;
}
