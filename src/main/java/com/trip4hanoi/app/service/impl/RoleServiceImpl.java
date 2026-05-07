package com.trip4hanoi.app.service.impl;


import com.trip4hanoi.app.dto.req.RoleRequest;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.RoleResponse;
import com.trip4hanoi.app.entity.Permission;
import com.trip4hanoi.app.entity.Role;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.RoleMapper;
import com.trip4hanoi.app.repository.PermissionRepository;
import com.trip4hanoi.app.repository.RedisAuthorityRepository;
import com.trip4hanoi.app.repository.RoleRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j(topic = "ROLE SERVICE")
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RedisAuthorityRepository redisAuthorityRepository;
    private final RoleMapper roleMapper;

    @Override
    public RoleResponse createRole(RoleRequest request) {
        log.info("Create new role: {}", request.getName());
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissions()));
        Role role = roleMapper.toRoleEntity(request);
        role.setPermissions(permissions);

        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    @Override
    public PageResponse<RoleResponse> getAllRoles(String keyword, String sort, int page, int size) {
        log.info("Fetching roles with keyword: {}, sort: {}, page: {}", keyword, sort, page);

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

        Page<Role> pageResult;
        if (StringUtils.hasLength(keyword)) {
            pageResult = roleRepository.searchByKeyword("%" + keyword + "%", pageable);
        } else {
            pageResult = roleRepository.findAll(pageable);
        }

        List<RoleResponse> responses = pageResult.getContent().stream()
                .map(roleMapper::toRoleResponse)
                .toList();

        return PageResponse.from(pageResult, responses);
    }

    @Override
    public RoleResponse updateRole(Long id, RoleRequest request) {
        log.info("Update role id: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissions()));
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(permissions);

        role = roleRepository.save(role);
        clearUserCacheByRole(id);

        return roleMapper.toRoleResponse(role);
    }

    @Override
    public void deleteRole(Long id) {
        log.info("Delete role id: {}", id);
        clearUserCacheByRole(id);
        roleRepository.deleteById(id);
    }

    @Override
    public List<RoleResponse> getAllList() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    private void clearUserCacheByRole(Long roleId) {
        List<User> affectedUsers = userRepository.findAllByRoleId(roleId);
        for (User user : affectedUsers) {
            redisAuthorityRepository.deleteById(user.getEmail());
        }
    }
}
