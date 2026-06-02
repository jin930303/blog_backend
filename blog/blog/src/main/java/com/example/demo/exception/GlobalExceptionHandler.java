package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidationException(MethodArgumentNotValidException e){
        Map<String,String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(),error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    // 인증실패
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String,String>> handleBadCredentials(BadCredentialsException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","아이디 또는 비밀번호가 일치하지 않습니다."));
    }

    // 차단된 계정
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String,String>> handleLocked(LockedException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message","차단된 계정입니다. 관리자에게 문의하세요"));
    }

    //존재하지 않는 리소스
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String >> handleIllegalArgument(IllegalArgumentException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message",e.getMessage()));
    }
    //서버 내부 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String ,String >>handleException(Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message","서버 내부 오류가 발생했습니다."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String ,String>> handleAccessDeniedException(AccessDeniedException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message",e.getMessage()));
    }
}
