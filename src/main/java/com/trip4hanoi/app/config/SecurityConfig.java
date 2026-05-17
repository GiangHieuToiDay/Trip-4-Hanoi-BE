package com.trip4hanoi.app.config;


import com.trip4hanoi.app.entity.RedisAuthority;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.repository.RedisAuthorityRepository;
import com.trip4hanoi.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtDecoderConfig jwtDecoderConfig;
    private final UserRepository userRepository;
    private final RedisAuthorityRepository redisAuthorityRepository;

    private final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/login-google",
            "/api/auth/refresh-token",
            "/login/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/ws/**",
            "/swagger-ui.html",
            "/api/auth/verify",
            "/api/auth/resend-verify"
    };

    private final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/places/**",
            "/api/events/**",
            "/api/posts/**",
            "/api/categories/**",
            "/api/comments/post/**",
            "/api/weather"
    };


    /**
     * Cấu hình Filter Chain để kiểm soát các yêu cầu HTTP đi vào hệ thống.
     * Nghiệp vụ: Tắt CSRF, bật CORS, phân loại Endpoint công khai/cần bảo mật.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // Quan trọng cho SockJS
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()

                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(customBearerTokenResolver())
                        .jwt(jwtConfigurer ->
                                jwtConfigurer.decoder(jwtDecoderConfig)
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter(userRepository,redisAuthorityRepository)))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                );

        return http.build();
    }

    /**
     * Cấu hình CORS (Cross-Origin Resource Sharing).
     * Nghiệp vụ: Cho phép các ứng dụng Frontend (như React/Vite tại port 5173)
     * có thể gửi yêu cầu đến Backend Backend (port 8080).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList(
//                "http://localhost:5173",
//                "http://localhost:5174",
//                "http://localhost:3000",
//                "http://localhost:5500",
//                "http://127.0.0.1:5500"
//        ));
        // Cho phép tất cả các nguồn để thuận tiện test từ file HTML local
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    /**
     * Tùy chỉnh cách Spring Security lấy Token từ yêu cầu.
     * Nghiệp vụ: Ngăn chặn việc nạp Token tự động cho API Refresh Token
     * để tránh xung đột khi hệ thống cố gắng xác thực một Access Token đã hết hạn.
     */
    @Bean
    public BearerTokenResolver customBearerTokenResolver(){
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        return request -> {
            String path = request.getRequestURI();
            if(path != null && (path.contains("/api/auth/refresh-token") || path.contains("/ws"))){
                return null;
            }
            return resolver.resolve(request);
        };
    }

    /**
     * PHÂN QUYỀN ĐỘNG tích hợp Redis Cache.
     * Nghiệp vụ quan trọng nhất: Chuyển đổi thông tin định danh (Email) trong JWT
     * thành danh sách quyền (Authorities) thực tế.
     * Luồng: Kiểm tra Redis -> Nếu có thì lấy luôn -> Nếu không có thì tìm DB -> Lưu Redis -> Trả về.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(UserRepository userRepository,
                                                                 RedisAuthorityRepository redisAuthorityRepository){
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String email = jwt.getSubject();

            //  Kiểm tra cache trong Redis để tăng tốc độ xử lý
            Optional<RedisAuthority> cachedAuth = redisAuthorityRepository.findById(email);
            if (cachedAuth.isPresent()) {
                return cachedAuth.get().getAuthorities().stream()
                        .map(auth -> (GrantedAuthority) new SimpleGrantedAuthority(auth))
                        .collect(Collectors.toList());
            }

            //  Nếu không có cache, truy vấn Database để lấy quyền mới nhất
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                List<String> authorityStrings = user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());

                // 3. Lưu lại vào Redis (TTL 30 phút) để dùng cho các request sau
                redisAuthorityRepository.save(RedisAuthority.builder()
                        .email(email)
                        .authorities(authorityStrings)
                        .expirationTime(1800L)
                        .build());

                return user.getAuthorities().stream()
                        .map(auth -> (GrantedAuthority) auth)
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();
        });
        return jwtAuthenticationConverter;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }



}
