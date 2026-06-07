package com.trip4hanoi.app.service.impl;


import com.trip4hanoi.app.common.AuthProvider;
import com.trip4hanoi.app.common.UserStatus;
import com.trip4hanoi.app.dto.req.ChangePasswordRequest;
import com.trip4hanoi.app.dto.req.UserCreateRequest;
import com.trip4hanoi.app.dto.req.UserUpdateRequest;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.UserResponse;
import com.trip4hanoi.app.entity.RedisToken;
import com.trip4hanoi.app.entity.Role;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.UserMapper;
import com.trip4hanoi.app.repository.RedisAuthorityRepository;
import com.trip4hanoi.app.repository.RedisTokenRepository;
import com.trip4hanoi.app.repository.RoleRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.CloudinaryService;
import com.trip4hanoi.app.service.MailService;
import com.trip4hanoi.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j(topic = "USER SERVICE")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @NonFinal
    @Value("${app.baseurl}")
    private String baseurl;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTokenRepository redisTokenRepository;
    private final RedisAuthorityRepository redisAuthorityRepository; // Thêm repo mới
    private final CloudinaryService cloudinaryService;
    private final MailService mailService;

    @Override
    public UserResponse createUser(UserCreateRequest user, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        log.info("USER CREATE REQUEST");
        ValidateDuplicateUser(user);

        User userEntity = userMapper.toEntity(user);
        if (user.getStatus() != null) {
            userEntity.setStatus(user.getStatus());
        } else {
            userEntity.setStatus(UserStatus.INACTIVE);
        }


        // Verify email
        UUID uuid = UUID.randomUUID();
        String htmlRegister = """
                <h2>Verify account</h2>
                <p>Click link below:</p>
                <a href=\"""" + baseurl + "/verify?token=" + uuid + """
                \">
                    Verify
                </a>
                """;
        mailService.sendMail(userEntity.getEmail(), "Verify Account", htmlRegister);
        userEntity.setVerificationCode(uuid.toString());
        userEntity.setVerificationExpiredAt(LocalDateTime.now().plusMinutes(15));
        userEntity.setEmailSentCount(1);
        userEntity.setEmailSentDate(LocalDate.now());

        userEntity.setProvider(AuthProvider.LOCAL);
        userEntity.setPassword(passwordEncoder.encode(user.getPassword()));

        // Xử lý upload avatar nếu có file
        if (file != null && !file.isEmpty()) {
            try {
              Map uploadResult = cloudinaryService.uploadFile(file);
              String avatarUrl = uploadResult.get("url").toString();
              userEntity.setAvatar(avatarUrl);
            } catch (Exception e) {
                log.error("Failed to upload avatar to Cloudinary", e);
            }
        }

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<Role> roles = roleRepository.findAllById(user.getRoles());
            userEntity.setRoles(new HashSet<>(roles));
        } else {
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() ->{
                        Role roleEntity = new Role();
                        roleEntity.setName("USER");
                        roleEntity.setDescription("Default User Role");
                        return roleRepository.save(roleEntity);
                    });
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            userEntity.setRoles(roles);
        }

        User saveUser = userRepository.save(userEntity);
        return userMapper.toUserResponse(saveUser);
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(String keyword, String sort, int page, int size) {
        log.info("Searching users with keyword: {}, sort: {}, page: {}", keyword, sort, page);

        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "id");
        if(StringUtils.hasLength(sort)){
            // Regex: username:asc hoặc email:desc
            Pattern pattern =  Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sort);
            if(matcher.find()){
                String col = matcher.group(1);
                String dir = matcher.group(3);
                order = new Sort.Order(dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, col);
            }
        }

        int pageNo = page > 0 ? page -1 : 0;
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        String searchKeyword = StringUtils.hasLength(keyword) ? keyword : null;
        Page<User> pageResult = userRepository.searchByKeyword(searchKeyword, pageable);

        List<UserResponse> userResponses = pageResult.getContent().stream()
                .map(userMapper::toUserResponse)
                .toList();


        return PageResponse.from(pageResult, userResponses);
    }

    @Override
    public UserResponse getUserById(Long id) {
        return userMapper.toUserResponse(getUser(id));
    }

    @Override
    @Transactional
    public void updateUser(UserUpdateRequest request, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        log.info("Update user with id {}", request.getId());
        User userEntity = getUser(request.getId());
        userMapper.updateUser(userEntity, request);
        
        // Ưu tiên xử lý file upload nếu có, ngược lại mới lấy URL từ request avatar (nếu có)
        if (file != null && !file.isEmpty()) {
            try {
                Map uploadResult = cloudinaryService.uploadFile(file);
                String avatarUrl = uploadResult.get("url").toString();
                userEntity.setAvatar(avatarUrl);
            } catch (Exception e) {
                log.error("Failed to upload avatar to Cloudinary during update", e);
            }
        } else if (request.getAvatar() != null) {
            userEntity.setAvatar(request.getAvatar());
        }

        // Cập nhật Roles nếu có gửi kèm
        if (request.getRoles() != null) {
            List<Role> roles = roleRepository.findAllById(request.getRoles());
            userEntity.setRoles(new HashSet<>(roles));
        }

        // Cập nhật password nếu có gửi (và đã được điền)
        if (StringUtils.hasText(request.getPassword())) {
            userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getIsLocationTrackingEnabled() != null) {
            userEntity.setIsLocationTrackingEnabled(request.getIsLocationTrackingEnabled());
        }

        userRepository.save(userEntity);
        
        // Nếu user bị khóa (INACTIVE), thu hồi ngay lập tức mọi token đang hoạt động
        if (userEntity.getStatus() == UserStatus.INACTIVE) {
            revokeAllUserTokens(userEntity.getEmail());
            log.info("User {} is now INACTIVE. All tokens revoked.", userEntity.getEmail());
        }

        // Xóa cache quyền trong Redis để User nhận quyền mới ngay lập tức
        redisAuthorityRepository.deleteById(userEntity.getEmail());
        
        log.info("User with id {} has been updated and cache cleared", request.getId());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Delete user with id {}", id);
        User user = getUser(id);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);

        // THU HỒI TOÀN BỘ TOKEN khi xóa/vô hiệu hóa user
        revokeAllUserTokens(user.getEmail());
        
        log.info("User with id {} has been deleted (status INACTIVE) and tokens revoked", id);

    }

    /**
     * Thu hồi toàn bộ token của user trong Redis
     * @param email
     */
    private void revokeAllUserTokens(String email) {
        List<RedisToken> userTokens = redisTokenRepository.findByEmail(email);
        if (userTokens != null && !userTokens.isEmpty()) {
            redisTokenRepository.deleteAll(userTokens);
        }
        redisAuthorityRepository.deleteById(email);
    }

    @Override
    public UserResponse getMyInfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(userEntity);
    }

    @Override
    @Transactional
    public void changeMyPassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));



        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }


        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);


        List<RedisToken> userTokens = redisTokenRepository.findByEmail(email);

        if (userTokens != null && !userTokens.isEmpty()) {
            redisTokenRepository.deleteAll(userTokens);
            // Xóa cả cache quyền để nạp lại sau khi đăng nhập lại
            redisAuthorityRepository.deleteById(email);
            log.info("Revoked {} tokens and cleared authority cache for user {} after password change.", userTokens.size(), email);
        } else {
            log.info("No active tokens found for user {} during password change.", email);
        }

        log.info("User {} changed their password successfully.", email);
    }

    /**
     * Validate duplicate email and username
     * @param request
     */
    private void ValidateDuplicateUser(UserCreateRequest request){
        String email = request.getEmail();
        String username = request.getUsername();

        if(userRepository.existsByEmail(email)){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        if(userRepository.existsByUsername(username)){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
    }

    private User getUser(long id){
        return userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
