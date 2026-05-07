package com.trip4hanoi.app.service.impl;


import com.trip4hanoi.app.dto.req.PermissionRequest;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PermissionResponse;
import com.trip4hanoi.app.entity.Permission;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;

import com.trip4hanoi.app.mapper.PermissionMapper;
import com.trip4hanoi.app.repository.PermissionRepository;
import com.trip4hanoi.app.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j(topic = "PERMISSION SERVICE")
@RequiredArgsConstructor
public class PermissionsServiceImpl implements PermissionService {
    private final PermissionMapper permissionMapper;
    private final PermissionRepository permissionRepository;



    @Override
    public PermissionResponse createPermission(PermissionRequest request) {
        log.info("Creating new permission: {}", request.getName());
        if (permissionRepository.findByName(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }
        Permission entity = permissionMapper.toPermissionEntity(request);
        return permissionMapper.toPermissionResponse(permissionRepository.save(entity));
    }

    @Override
    public PageResponse<PermissionResponse> getAllPermissions(String keyword, String sort, int page, int size) {
        log.info("Fetching all permissions with keyword: {}, sort: {}, page: {}", keyword, sort, page);

        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "id");
        if (StringUtils.hasLength(sort)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sort);
            if (matcher.find()) {
                String col = matcher.group(1);
                String dir = matcher.group(3);
                order = new Sort.Order(dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, col);
            }
        }

        int pageNo = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        Page<Permission> pageResult;
        if (StringUtils.hasLength(keyword)) {
            pageResult = permissionRepository.searchByKeyword("%" + keyword + "%", pageable);
        } else {
            pageResult = permissionRepository.findAll(pageable);
        }

        List<PermissionResponse> responses = pageResult.getContent().stream().map(permissionMapper::toPermissionResponse)
                .toList();

        return PageResponse.from(pageResult, responses);
    }

    @Override
    public PermissionResponse updatePermission(Long id, PermissionRequest request) {
        log.info("Updating permission ID: {}", id);
        Permission entity = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        
        return permissionMapper.toPermissionResponse(permissionRepository.save(entity));
    }

    @Override
    public void deletePermission(Long id) {
        log.info("Deleting permission with ID: {}", id);
        if (!permissionRepository.existsById(id)) {
            throw new AppException(ErrorCode.PERMISSION_NOT_FOUND);
        }
        permissionRepository.deleteById(id);
    }
}
