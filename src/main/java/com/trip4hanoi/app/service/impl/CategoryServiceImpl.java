package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.req.CategoryRequest;
import com.trip4hanoi.app.dto.res.CategoryResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.entity.Category;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.CategoryMapper;
import com.trip4hanoi.app.repository.CategoryRepository;
import com.trip4hanoi.app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CATEGORY-SERVICE")
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating new category: {}", request.getName());
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.CATEGORY_NAME_IS_EXIST);
        }
        Category category = categoryMapper.toCategoryEntity(request);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.info("Updating category ID {}: {}", id, request.getName());
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        
        category.setName(request.getName());
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category ID {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public PageResponse<CategoryResponse> getAllCategories(String keyword, String sort, int page, int size) {

        Sort.Order order = new Sort.Order(Sort.Direction.DESC, "id");
        if (StringUtils.hasLength(sort) && sort.contains(":")) {
            String[] parts = sort.split(":");
            order = new Sort.Order(parts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, parts[0]);
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(order));


        Page<Category> pageResult = categoryRepository.searchByName(keyword, pageable);
             List<CategoryResponse> responses = pageResult.getContent().stream().map(categoryMapper::toCategoryResponse).toList();
             return PageResponse.from(pageResult, responses);
    }
}
