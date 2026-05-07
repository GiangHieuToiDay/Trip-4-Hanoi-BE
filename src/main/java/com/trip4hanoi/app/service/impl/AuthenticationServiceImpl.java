package com.trip4hanoi.app.service.impl;


import com.nimbusds.jose.JOSEException;
import com.trip4hanoi.app.common.UserStatus;
import com.trip4hanoi.app.dto.JwtInfo;
import com.trip4hanoi.app.dto.TokenPayload;
import com.trip4hanoi.app.dto.req.LoginRequest;
import com.trip4hanoi.app.dto.req.RefreshTokenRequest;
import com.trip4hanoi.app.dto.res.LoginResponse;
import com.trip4hanoi.app.dto.res.PermissionResponse;
import com.trip4hanoi.app.dto.res.RoleResponse;
import com.trip4hanoi.app.dto.res.UserResponse;
import com.trip4hanoi.app.entity.RedisAuthority;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.repository.RedisAuthorityRepository;
import com.trip4hanoi.app.repository.RedisTokenRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.AuthenticationService;
import com.trip4hanoi.app.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j(topic = "AUTHENTICATION SERVICE")
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final ObjectProvider<AuthenticationManager> authenticationManagerProvider;
    private final JwtService jwtService;
    private final RedisTokenRepository redisTokenRepository;
    private final RedisAuthorityRepository redisAuthorityRepository; // Thêm repository mới
    private final UserRepository userRepository;


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
                .username(user.getUsername())
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
    public void logout(String token) throws ParseException {
        JwtInfo jwtInfo = jwtService.parseToken(token);
        String jwtId = jwtInfo.getJwtId();
        Date expiredTime = jwtInfo.getExpiredTime();

        if (jwtId == null || expiredTime.before(new Date())) {
            log.warn("Logout attempt with invalid or expired token.");
            return;
        }

        redisTokenRepository.deleteById(jwtId);
        log.info("Logout success for token: {}", jwtId);
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
}
