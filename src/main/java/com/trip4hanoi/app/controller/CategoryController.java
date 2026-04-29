package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.CategoryResponse;
import com.trip4hanoi.app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    //@Operation(summary = "Get all categories", description = "API get all categories")
    @GetMapping
    public ResponseEntity<APIResponse<List<CategoryResponse>>> getAllCategories() {

        List<CategoryResponse> categories = categoryService.getAllCategories();

        APIResponse<List<CategoryResponse>> response = APIResponse.<List<CategoryResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully retrieved categories")
                .data(categories)
                .build();

        return ResponseEntity.ok(response);
    }
}
