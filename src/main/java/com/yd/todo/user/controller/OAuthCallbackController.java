package com.yd.todo.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yd.todo.global.common.ApiResponse;
import com.yd.todo.user.model.dto.LoginResponse;
import com.yd.todo.user.model.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OAuthCallbackController {

    private final UserService userService;

    @GetMapping("/oauth/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(@RequestParam("code") String code) {
        LoginResponse response = userService.kakaoLogin(code);
        return ResponseEntity.ok(ApiResponse.success(200, "로그인 성공", response));
    }
}