package com.yd.todo.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yd.todo.global.auth.LoginUser;
import com.yd.todo.global.common.ApiResponse;
import com.yd.todo.user.model.dto.LoginResponse;
import com.yd.todo.user.model.dto.UserResponse;
import com.yd.todo.user.model.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	
	@PostMapping("/login/kakao")
	public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(@RequestParam(value = "code", required = true) String code) {
	    return ResponseEntity.ok().body(ApiResponse.success(200, "로그인 성공", userService.kakaoLogin(code)));
	}

	@PostMapping("/login/google")
	public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(@RequestParam(value = "code", required = true) String code) {
	    return ResponseEntity.ok().body(ApiResponse.success(200, "로그인 성공", userService.googleLogin(code)));
	}

	@GetMapping("/mypage")
	public ResponseEntity<ApiResponse<UserResponse>> findUserInfo(@AuthenticationPrincipal LoginUser loginUser) {
		return ResponseEntity.ok().body(ApiResponse.success(200, "유저정보 조회 성공", userService.findUserInfo(loginUser.getUserId())));
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> withdraw(@AuthenticationPrincipal LoginUser loginUser) {
		userService.withdraw(loginUser.getUserId());
		return ResponseEntity.ok().body(ApiResponse.success(200, "회원 탈퇴 완료", null));
	}

}
