package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.CategoryRequest;
import com.trip4hanoi.app.dto.res.CategoryResponse;
import com.trip4hanoi.app.dto.res.PageResponse;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
    CategoryResponse getCategoryById(Long id);
    PageResponse<CategoryResponse> getAllCategories(String keyword, String sort, int page, int size);
}
