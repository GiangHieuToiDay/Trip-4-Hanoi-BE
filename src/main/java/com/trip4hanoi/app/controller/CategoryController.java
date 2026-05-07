package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.CategoryRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.CategoryResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j(topic = "CATEGORY-CONTROLLER")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<CategoryResponse>>> getAllCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id:desc") String sort
    ) {
        log.info("REST request to get all categories - keyword: {}, page: {}", keyword, page);
        var result = categoryService.getAllCategories(keyword, sort, page, size);

        return ResponseEntity.ok(APIResponse.<PageResponse<CategoryResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully retrieved categories")
                .data(result)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        var result = categoryService.getCategoryById(id);
        return ResponseEntity.ok(APIResponse.<CategoryResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .data(result)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_CATEGORY')")
    public ResponseEntity<APIResponse<CategoryResponse>> createCategory(@RequestBody CategoryRequest request) {
        var result = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.<CategoryResponse>builder()
                .status(HttpStatus.CREATED.value())
                .code(1000)
                .message("Category created successfully")
                .data(result)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CATEGORY')")
    public ResponseEntity<APIResponse<CategoryResponse>> updateCategory(@PathVariable Long id, @RequestBody CategoryRequest request) {
        var result = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(APIResponse.<CategoryResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Category updated successfully")
                .data(result)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CATEGORY')")
    public ResponseEntity<APIResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Category deleted successfully")
                .build());
    }
}
