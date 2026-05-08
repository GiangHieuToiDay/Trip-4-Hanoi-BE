package com.trip4hanoi.app.config;

import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Xử lý các yêu cầu không có JWT hoặc JWT đã hết hạn/không hợp lệ.
 * Nghiệp vụ: Chuyển đổi phản hồi mặc định của Spring (thường là 403 HTML)
 * sang JSON định dạng chuẩn APIResponse (401 Unauthorized).
 */
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    /**
     * Phương thức được gọi khi xảy ra lỗi xác thực (AuthenticationException).
     * Nghiệp vụ: Trả về JSON chứa mã lỗi  (UNAUTHENTICATED)
     * để Frontend có thể điều hướng người dùng quay lại trang Đăng nhập.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // Mặc định là lỗi chưa xác thực
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        
        // Kiểm tra xem lỗi có phải do Token hết hạn không (thông qua attribute được đặt bởi JwtDecoder)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authException.getMessage().contains("Jwt expired")) {
            errorCode = ErrorCode.TOKEN_EXPIRED;
        }

        // Thiết lập mã trạng thái HTTP và định dạng JSON
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Tạo cấu trúc phản hồi API thống nhất (APIResponse)
        APIResponse<Void> apiResponse = APIResponse.<Void>builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        // Chuyển đổi đối tượng APIResponse sang chuỗi JSON và ghi vào Body của phản hồi
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }
}
