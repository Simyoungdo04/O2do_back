package com.yd.todo.global.exception;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.yd.todo.global.common.ApiResponse;
import com.yd.todo.global.exception.dailylist.DailyListNotFoundException;
import com.yd.todo.global.exception.todo.TodoAccessDeniedException;
import com.yd.todo.global.exception.todo.TodoNotFoundException;
import com.yd.todo.global.exception.token.InvalidRefreshTokenException;
import com.yd.todo.global.exception.token.RefreshTokenExpiredException;
import com.yd.todo.global.exception.user.UserNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- 도메인 예외 (Not Found 계열 → 404) ----------

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException e) {
        log.warn("UserNotFound: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler(DailyListNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleDailyListNotFound(DailyListNotFoundException e) {
        log.warn("DailyListNotFound: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTodoNotFound(TodoNotFoundException e) {
        log.warn("TodoNotFound: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    // ---------- 인가 실패 (403) ----------

    @ExceptionHandler(TodoAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleTodoAccessDenied(TodoAccessDeniedException e) {
        log.warn("TodoAccessDenied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(HttpStatus.FORBIDDEN.value(), e.getMessage()));
    }

    // ---------- 토큰 관련 (401) ----------

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRefreshToken(InvalidRefreshTokenException e) {
        log.warn("InvalidRefreshToken: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenExpired(RefreshTokenExpiredException e) {
        log.warn("RefreshTokenExpired: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
    }

    // ---------- 요청 형식 오류 (400) ----------

    // @Valid 검증 실패 (예: title 누락 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidation(
            MethodArgumentNotValidException e, HandlerMethod handlerMethod) {

        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(err -> Map.of("field", err.getField(), "message", String.valueOf(err.getDefaultMessage())))
                .toList();

        Map<String, Object> payload = Map.of(
                "controller", handlerMethod.getBeanType().getSimpleName(),
                "method", handlerMethod.getMethod().getName(),
                "errors", errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), payload.toString()));
    }

    // URL 파라미터 타입 불일치 (예: /api/todos/abc → id 는 Long 이어야 함)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = String.format("'%s' 파라미터의 형식이 잘못되었습니다.", e.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), message));
    }

    // JSON 파싱 자체가 깨진 경우
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), "요청 본문의 형식이 잘못되었습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    // ---------- 존재하지 않는 URL / 잘못된 메서드 (404 / 405) ----------

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "요청한 URL을 찾을 수 없습니다."));
    }

    // ---------- 마지막 방어선 (500) ----------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("예상하지 못한 서버 오류", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 오류가 발생했습니다."));
    }
}