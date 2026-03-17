package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.res.CategoryResponse;
import com.trip4hanoi.app.mapper.CategoryMapper;
import com.trip4hanoi.app.repository.CategoryRepository;
import com.trip4hanoi.app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }
}
