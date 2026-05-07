package com.trip4hanoi.app.config;


import com.trip4hanoi.app.entity.RedisToken;
import com.trip4hanoi.app.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Optional;

/**
 * Cấu hình giải mã và xác thực chi tiết cho JSON Web Token (JWT).
 * Nghiệp vụ: Thực hiện kiểm tra đồng thời cả 
 * 1. Chữ ký JWT (Stateless - Nimbus)
 * 2. Trạng thái thu hồi (Stateful - Redis)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtDecoderConfig implements JwtDecoder {

    private final RedisTokenRepository redisTokenRepository;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Value("${application.security.jwt.secret-key}")
    private String SECRET_KEY;

    /**
     * Khởi tạo bộ giải mã sau khi đã nạp được SECRET_KEY từ cấu hình.
     * Nghiệp vụ: Thiết lập thuật toán HS512 và cấu hình Timestamp (ngày hết hạn).
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA512");
        nimbusJwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
        // Cấu hình kiểm tra thời gian hết hạn (Timestamp)
        nimbusJwtDecoder.setJwtValidator(new JwtTimestampValidator(Duration.ZERO));
    }


    /**
     * Phương thức giải mã Token chính.
     * Nghiệp vụ: 
     * 1. Kiểm tra chữ ký và cấu trúc Token xem có bị thay đổi trái phép không.
     * 2. Kiểm tra JWT ID (jti) trong Redis để xác nhận người dùng đã nhấn Logout chưa (Revocation Check).
     */
    @Override
    public Jwt decode(String token) throws JwtException {
        log.debug(">>>>Decoding JWT Token");

        Jwt jwt;
        try {
            //  Giải mã Nimbus (Xác thực chữ ký + Thời gian hết hạn)
            jwt = nimbusJwtDecoder.decode(token);
        }
        catch  (JwtException e) {
            log.warn("Stateless JWT validation failed: {}", e.getMessage());
            throw e;
        }

        //  Kiểm tra trạng thái trong Redis (Stateful Validation)
        String jwtId = jwt.getClaimAsString("jti");
        if (jwtId == null) {
            log.warn("JWT does not contain 'jti' (JWT ID) claim.");
            throw new JwtException("Token is invalid (missing jti).");
        }
        
        // Tìm kiếm JWT ID trong Redis (bảng redistokens)
        Optional<RedisToken> redisTokenOpt = redisTokenRepository.findById(jwtId);

        if (redisTokenOpt.isEmpty()) {
            // Nếu không tìm thấy trong Redis nghĩa là Token đã bị thu hồi do Logout hoặc Đổi mật khẩu
            log.warn("Token JTI {} not found in Redis. Token is revoked.", jwtId);
            throw new JwtException("Token has been revoked or is invalid.");
        }


        log.debug("Token verified successfully (stateless + stateful) for JTI: {}", jwtId);
        return jwt;
    }

}
