package com.yd.todo.global.token.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yd.todo.global.auth.LoginUser;
import com.yd.todo.global.common.ApiResponse;
import com.yd.todo.global.token.model.dto.TokenResponse;
import com.yd.todo.global.token.model.service.TokenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    // 액세스 토큰 만료 시 리프레시 토큰으로 재발급
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @RequestParam(value = "refreshToken", required = true) String refreshToken) {

        Map<String, String> tokens = tokenService.tokenRotation(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(200, "토큰 재발급 성공", TokenResponse.from(tokens)));
    }

    // 로그아웃: 리프레시 토큰 DB에서 제거
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(value = "refreshToken", required = true) String refreshToken) {

        tokenService.logout(refreshToken, loginUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(200, "로그아웃 성공", null));
    }
}