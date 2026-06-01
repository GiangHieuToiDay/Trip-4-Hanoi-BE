package com.trip4hanoi.app.exception;

import com.trip4hanoi.app.dto.res.APIResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@Slf4j(topic = "GLOBAL EXCEPTION")
public class GlobalExceptionHandler {


    //  Handle lỗi sai Method (Fix lỗi "Unsupported methods" 405 thay vì 401)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<APIResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                APIResponse.builder()
                        .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                        .code(405)
                        .message("Method not allowed: " + e.getMethod())
                        .build()
        );
    }

    //  Handle lỗi dữ liệu đầu vào (Fix lỗi ép kiểu, lỗi mảng rỗng, sai format)
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            HttpMediaTypeNotSupportedException.class,
            ServletRequestBindingException.class,
            MaxUploadSizeExceededException.class
    })
    public ResponseEntity<APIResponse<Object>> handleBadRequestException(Exception e) {
        log.warn("Bad Request Exception: {}", e.getMessage());
        String message = "Invalid request format or missing parameters";
        int code = ErrorCode.INVALID_KEY.getCode();

        if (e instanceof MethodArgumentTypeMismatchException mismatch) {
            message = String.format("Parameter '%s' expects type '%s'",
                    mismatch.getName(), Objects.requireNonNull(mismatch.getRequiredType()).getSimpleName());
        } else if (e instanceof ServletRequestBindingException) {
            message = "Missing or invalid required header/parameter: " + e.getMessage();
            code = 400;
        } else if (e instanceof MaxUploadSizeExceededException) {
            message = "File size exceeds the limit";
            code = 400;
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                APIResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code(code)
                        .message(message)
                        .build()
        );
    }

    //  Handle lỗi Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Object>> handleValidation(MethodArgumentNotValidException e) {
        var fieldError = e.getFieldError();
        String enumKey = (fieldError != null) ? fieldError.getDefaultMessage() : "INVALID_KEY";
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
            var constraint = e.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);
            attributes = constraint.getConstraintDescriptor().getAttributes();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    APIResponse.builder().status(400).code(400).message(enumKey).build()
            );
        }

        return ResponseEntity.status(errorCode.getStatus()).body(
                APIResponse.builder()
                        .status(errorCode.getStatus().value())
                        .code(errorCode.getCode())
                        .message(Objects.nonNull(attributes) ? mapAttribute(errorCode.getMessage(), attributes) : errorCode.getMessage())
                        .build()
        );
    }

    //  Handle App Exception & Access Denied
    @ExceptionHandler(AppException.class)
    public ResponseEntity<APIResponse<Object>> handleAppException(AppException ex) {
        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(
                APIResponse.builder()
                        .status(ex.getErrorCode().getStatus().value())
                        .code(ex.getErrorCode().getCode())
                        .message(ex.getErrorCode().getMessage())
                        .build()
        );
    }

    @ExceptionHandler({AccessDeniedException.class, org.springframework.security.authorization.AuthorizationDeniedException.class})
    public ResponseEntity<APIResponse<Object>> handleAccessDenied(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                APIResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .code(ErrorCode.UNAUTHORIZED.getCode())
                        .message(ErrorCode.UNAUTHORIZED.getMessage())
                        .build()
        );
    }

    private String mapAttribute(String message, Map<String, Object> attribute) {
        if (attribute == null) return message;
        for (Map.Entry<String, Object> entry : attribute.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return message;
    }
}