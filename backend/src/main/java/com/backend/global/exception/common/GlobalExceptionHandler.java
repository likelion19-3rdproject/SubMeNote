package com.backend.global.exception.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity
                .status(code.getStatus())
                .body(ErrorResponse.of(code));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(SecurityException e) {
        return ResponseEntity
                .status(403)
                .body(ErrorResponse.of("FORBIDDEN", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        StringBuilder message = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            if (message.length() > 0) {
                message.append(", ");
            }
            message.append(error.getField()).append(": ").append(error.getDefaultMessage());
        });
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", message.toString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        e.printStackTrace(); // 디버깅을 위해 스택 트레이스 출력
        
        // 더 자세한 에러 정보 제공
        String message = "서버 오류가 발생했습니다.";
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            message = e.getMessage();
        } else if (e.getCause() != null && e.getCause().getMessage() != null) {
            message = e.getCause().getMessage();
        } else {
            message = e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : "알 수 없는 오류");
        }
        
        return ResponseEntity
                .internalServerError()
                .body(ErrorResponse.of("INTERNAL_SERVER_ERROR", message));
    }
}
