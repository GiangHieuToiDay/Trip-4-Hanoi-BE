package com.trip4hanoi.app.exception;


import com.trip4hanoi.app.dto.res.APIResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@Slf4j(topic = "GLOBAL EXCEPTION")
public class GlobalExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<APIResponse> handleRuntimeException(RuntimeException e){
        log.error("Unexpected error", e);
        APIResponse apiResponse = APIResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message("An internal server error occurred. Please contact support.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        String enumKey = e.getFieldError().getDefaultMessage();
        ErrorCode errorCode  = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
            var constraintViolationException  = e.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);
            attributes = constraintViolationException.getConstraintDescriptor().getAttributes();
        }
        catch (Exception ex) {
            log.error("Validation error mapping failed: {}", ex.getMessage());
        }

        APIResponse response = APIResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(Objects.nonNull(attributes) ?
                        mapAttribute(errorCode.getMessage(),attributes)
                        : errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

//    @ExceptionHandler(value = {
//            AccessDeniedException.class,
//            org.springframework.security.authorization.AuthorizationDeniedException.class
//    })
//    public ResponseEntity<APIResponse> handleAccessDeniedException(Exception ex) {
//        log.warn("Access Denied: {}", ex.getMessage());
//        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
//
//        APIResponse response = APIResponse.builder()
//                .status(errorCode.getStatus().value())
//                .code(errorCode.getCode())
//                .message(errorCode.getMessage())
//                .build();
//
//        return ResponseEntity.status(errorCode.getStatus()).body(response);
//    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<APIResponse> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        APIResponse response = APIResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    private String mapAttribute(String message , Map<String, Object> attribute){
        if(attribute == null || attribute.isEmpty()){
            return message;
        }
        if(attribute.containsKey("min")) {
            String minValue = String.valueOf(attribute.get("min"));
            message = message.replace("{min}", minValue);
        }
        if(attribute.containsKey("max")) {
            String maxValue = String.valueOf(attribute.get("max"));
            message = message.replace("{max}", maxValue);
        }
        return message;
    }

}

