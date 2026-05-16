package com.trip4hanoi.app.service.impl;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nimbusds.jose.JOSEException;
import com.trip4hanoi.app.common.AuthProvider;
import com.trip4hanoi.app.common.UserStatus;
import com.trip4hanoi.app.dto.JwtInfo;
import com.trip4hanoi.app.dto.TokenPayload;
import com.trip4hanoi.app.dto.req.GoogleLoginRequest;
import com.trip4hanoi.app.dto.req.LoginRequest;
import com.trip4hanoi.app.dto.req.RefreshTokenRequest;
import com.trip4hanoi.app.dto.res.LoginResponse;
import com.trip4hanoi.app.dto.res.PermissionResponse;
import com.trip4hanoi.app.dto.res.RoleResponse;
import com.trip4hanoi.app.dto.res.UserResponse;
import com.trip4hanoi.app.entity.RedisAuthority;
import com.trip4hanoi.app.entity.Role;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.repository.RedisAuthorityRepository;
import com.trip4hanoi.app.repository.RedisTokenRepository;
import com.trip4hanoi.app.repository.RoleRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.AuthenticationService;
import com.trip4hanoi.app.service.JwtService;
import com.trip4hanoi.app.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j(topic = "AUTHENTICATION SERVICE")
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    @NonFinal
    @Value("${app.baseurl}")
    private String baseurl;

    @NonFinal
    @Value("${google.client-id}")
    private String clientId;

    private final ObjectProvider<AuthenticationManager> authenticationManagerProvider;
    private final JwtService jwtService;
    private final RedisTokenRepository redisTokenRepository;
    private final RedisAuthorityRepository redisAuthorityRepository; // Thêm repository mới
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MailService mailService;


    /**
     * Xác thực user và tạo JWT token (accessToken & refreshToken)
     *
     * @param loginRequest
     * @return
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword());

        AuthenticationManager authenticationManager = authenticationManagerProvider.getObject();
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        User user = (User) authenticate.getPrincipal();

        // Lưu sẵn quyền vào Redis ngay khi Login để các request sau không cần query DB
        List<String> authorityStrings = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        redisAuthorityRepository.save(RedisAuthority.builder()
                .email(user.getEmail())
                .authorities(authorityStrings)
                .expirationTime(1800L) // Cache trong 30 phút
                .build());

        TokenPayload accessToken = jwtService.generateAccessToken(user);
        TokenPayload refreshToken = jwtService.generateRefreshToken(user);

        Set<RoleResponse> roleResponses = new HashSet<>();
        user.getRoles().forEach(role -> {
            Set<PermissionResponse> permissionResponses = new HashSet<>();

            if (role.getPermissions() != null) {
                role.getPermissions().forEach(permission -> {
                    permissionResponses.add(PermissionResponse.builder()
                            .name(permission.getName())
                            .description(permission.getDescription())
                            .build());
                });
            }

            roleResponses.add(RoleResponse.builder()
                    .name(role.getName())
                    .description(role.getDescription())
                    .permissions(permissionResponses)
                    .build());

        });

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getActualUsername())
                .email(user.getEmail())
                .roles(roleResponses)
                .provider(user.getProvider())
                .status(user.getStatus())
                .build();

        return LoginResponse.builder()
                .user(userResponse)
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    /**
     * logout user và vô hiệu hóa TẤT CẢ token (Access & Refresh) trong Redis
     *
     *
     * @param token
     * @throws ParseException
     */
    @Override
    public void logout(String token) {
        try {
            JwtInfo jwtInfo = jwtService.parseToken(token);
            String jwtId = jwtInfo.getJwtId();
            Date expiredTime = jwtInfo.getExpiredTime();

            if (jwtId == null || expiredTime.before(new Date())) {
                log.warn("Logout attempt with invalid or expired token.");
                return;
            }

            redisTokenRepository.deleteById(jwtId);
            log.info("Logout success for token: {}", jwtId);
        } catch (ParseException e) {
            log.error("Failed to parse token during logout: {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    /**
     * tạo accessToken mới
     *
     * @param token
     * @return
     * @throws ParseException
     * @throws JOSEException
     */
    @Override
    public LoginResponse refreshToken(RefreshTokenRequest token) throws ParseException, JOSEException {
        //  Xác thực Refresh Token hiện tại
        if (!jwtService.verifyToken(token.getRefreshToken())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        JwtInfo jwtInfo = jwtService.parseToken(token.getRefreshToken());
        String email = jwtService.extractEmail(token.getRefreshToken());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra trạng thái User
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // THU HỒI Refresh Token cũ (Rotation)
        String oldJti = jwtInfo.getJwtId();
        redisTokenRepository.deleteById(oldJti);
        log.info("Old Refresh Token {} revoked for rotation.", oldJti);

        // Tạo cặp Token MỚI
        TokenPayload newAccessToken = jwtService.generateAccessToken(user);
        TokenPayload newRefreshToken = jwtService.generateRefreshToken(user);

        log.info("Tokens rotated successfully for user: {}", user.getEmail());

        return LoginResponse.builder()
                .accessToken(newAccessToken.getToken())
                .refreshToken(newRefreshToken.getToken())
                .build();
    }


    /**
     * Resend verify email
     *
     * @param email
     */
    @Override
    public void resendVerifyMail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        LocalDate today = LocalDate.now();

        // reset count nếu sang ngày mới
        if (user.getEmailSentDate() == null
                || !user.getEmailSentDate().equals(today)) {

            user.setEmailSentDate(today);
            user.setEmailSentCount(0);
        }

        // giới hạn 5 mail/ngày
        if (user.getEmailSentCount() >= 5) {
            throw new AppException(ErrorCode.EMAIL_LIMIT_EXCEEDED);
        }

        UUID uuid = UUID.randomUUID();

        String htmlRegister = """
            <h2>Verify account</h2>
            <p>Click link below:</p>
            <a href=\"""" + baseurl + "/api/auth/verify?token=" + uuid + """
            \">
                Verify
            </a>
            """;

        mailService.sendMail(user.getEmail(), "Verify Account", htmlRegister);

        // token mới sẽ ghi đè token cũ
        user.setVerificationCode(uuid.toString());
        user.setVerificationExpiredAt(LocalDateTime.now().plusMinutes(15));

        user.setEmailSentCount(user.getEmailSentCount() + 1);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public LoginResponse loginGoogle(
            GoogleLoginRequest request
    ) {

        GoogleIdToken.Payload payload;

        try {

            GoogleIdTokenVerifier verifier =
                    new GoogleIdTokenVerifier.Builder(
                            new NetHttpTransport(),
                            GsonFactory.getDefaultInstance()
                    )
                            .setAudience(Collections.singletonList(clientId))
                            .build();

            GoogleIdToken idToken =
                    verifier.verify(request.getIdToken());

            if (idToken == null) {
                log.error("Invalid Google ID Token");
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            payload = idToken.getPayload();

        } catch (Exception e) {
            log.error("Error verifying Google ID Token: {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String email = payload.getEmail();
        String googleId = payload.getSubject(); // Lấy unique ID từ Google

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // TRƯỜNG HỢP 1: User mới hoàn toàn -> Đăng ký mới với provider GOOGLE
                    User newUser = User.builder()
                            .email(email)
                            .username((String) payload.get("name"))
                            .avatar((String) payload.get("picture"))
                            .provider(AuthProvider.GOOGLE)
                            .providerId(googleId)
                            .status(UserStatus.ACTIVE)
                            .isVerified(true)
                            .password("") // Google login không dùng password
                            .build();

                    Role userRole = roleRepository
                            .findByName("USER")
                            .orElseThrow(() ->
                                    new RuntimeException("ROLE USER NOT FOUND")
                            );

                    newUser.setRoles(Set.of(userRole));

                    return userRepository.save(newUser);
                });
        
        // TRƯỜNG HỢP 2: User đã tồn tại (có thể là LOCAL hoặc GOOGLE)
        boolean needUpdate = false;

        // Nếu user chưa có providerId (đăng ký LOCAL trước đó) thì liên kết thêm ID Google
        if (user.getProviderId() == null || user.getProviderId().isEmpty()) {
            user.setProviderId(googleId);
            needUpdate = true;
        }

        // Không ghi đè provider: Nếu là LOCAL thì giữ nguyên LOCAL, nếu là GOOGLE thì giữ GOOGLE
        // Chỉ cập nhật trạng thái nếu chưa verified (vì Google đã tin cậy rồi)
        if (!user.getIsVerified()) {
            user.setIsVerified(true);
            user.setStatus(UserStatus.ACTIVE);
            needUpdate = true;
        }
        
        if (needUpdate) {
            user = userRepository.save(user);
        }

        // Lưu sẵn quyền vào Redis
        List<String> authorityStrings = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        redisAuthorityRepository.save(RedisAuthority.builder()
                .email(user.getEmail())
                .authorities(authorityStrings)
                .expirationTime(1800L)
                .build());

        TokenPayload accessToken = jwtService.generateAccessToken(user);
        TokenPayload refreshToken = jwtService.generateRefreshToken(user);

        Set<RoleResponse> roleResponses = new HashSet<>();
        user.getRoles().forEach(role -> {
            Set<PermissionResponse> permissionResponses = new HashSet<>();
            if (role.getPermissions() != null) {
                role.getPermissions().forEach(permission -> {
                    permissionResponses.add(PermissionResponse.builder()
                            .name(permission.getName())
                            .description(permission.getDescription())
                            .build());
                });
            }
            roleResponses.add(RoleResponse.builder()
                    .name(role.getName())
                    .description(role.getDescription())
                    .permissions(permissionResponses)
                    .build());
        });

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getActualUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .roles(roleResponses)
                .provider(user.getProvider())
                .status(user.getStatus())
                .build();

        return LoginResponse.builder()
                .user(userResponse)
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {

        User user = userRepository
                .findByVerificationCode(token);
        if( user == null ) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // check hết hạn
        if (user.getVerificationExpiredAt() == null
                || user.getVerificationExpiredAt()
                .isBefore(LocalDateTime.now())) {

            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        // verify thành công
        user.setVerificationCode(null);
        user.setVerificationExpiredAt(null);
        user.setIsVerified(true);

        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);
    }

}
