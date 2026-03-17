package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.res.CategoryResponse;
import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
}
