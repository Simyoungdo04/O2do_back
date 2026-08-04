package com.yd.todo.user.model.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.todo.global.exception.user.UserNotFoundException;
import com.yd.todo.global.token.service.TokenService;
import com.yd.todo.user.model.dto.LoginResponse;
import com.yd.todo.user.model.dto.UserResponse;
import com.yd.todo.user.model.entity.User;
import com.yd.todo.user.model.repository.UserRepository;
import com.yd.todo.user.model.vo.KakaoUserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KakaoService kakaoService;
    private final TokenService tokenService;

    // 카카오 로그인 = 신규면 가입, 기존이면 로그인 (인가코드 하나로 전부 처리)
    @Transactional
    public LoginResponse kakaoLogin(String code) {
    	String kakaoAccessToken = kakaoService.getKakaoAccessToken(code);
        KakaoUserInfo info = kakaoService.getUserInfo(kakaoAccessToken);

        User user = userRepository
                .findByProviderAndProviderId("KAKAO", info.getProviderId())
                .map(existing -> {
                	existing.updateProfile(info.getEmail(), info.getUserName());   // 재로그인 시 닉네임 갱신
                    return existing;
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .provider("KAKAO")
                        .providerId(info.getProviderId())
                        .userName(info.getUserName())
                        .build()));

        Map<String, String> tokens = tokenService.getTokens(user.getId());

        return LoginResponse.builder()
                .accessToken(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .user(UserResponse.from(user))
                .build();
    }

    // 마이페이지 조회
    public UserResponse findUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));
        return UserResponse.from(user);
    }
}