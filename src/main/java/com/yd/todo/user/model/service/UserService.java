package com.yd.todo.user.model.service;

import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.todo.dailyList.model.repository.DailyListRepository;
import com.yd.todo.global.exception.user.DuplicateLoginIdException;
import com.yd.todo.global.exception.user.InvalidCredentialsException;
import com.yd.todo.global.exception.user.UserNotFoundException;
import com.yd.todo.global.token.model.repository.RefreshTokenRepository;
import com.yd.todo.global.token.model.service.TokenService;
import com.yd.todo.todo.model.repository.TodoRepository;
import com.yd.todo.user.model.dto.LocalLoginRequest;
import com.yd.todo.user.model.dto.LoginResponse;
import com.yd.todo.user.model.dto.SignupRequest;
import com.yd.todo.user.model.dto.UserResponse;
import com.yd.todo.user.model.entity.User;
import com.yd.todo.user.model.repository.UserRepository;
import com.yd.todo.user.model.vo.GoogleUserInfo;
import com.yd.todo.user.model.vo.KakaoUserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KakaoService kakaoService;
    private final GoogleService googleService;
    private final TokenService tokenService;
    private final TodoRepository todoRepository;
    private final DailyListRepository dailyListRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    // 자체 회원가입 = LOCAL provider 로 신규 생성 후 즉시 로그인 처리
    @Transactional
    public LoginResponse signup(SignupRequest request) {
        if (userRepository.findByProviderAndProviderId("LOCAL", request.getLoginId()).isPresent()) {
            throw new DuplicateLoginIdException("이미 사용 중인 아이디입니다.");
        }

        User user = userRepository.save(User.builder()
                .provider("LOCAL")
                .providerId(request.getLoginId())
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .build());

        Map<String, String> tokens = tokenService.getTokens(user.getId());

        return LoginResponse.builder()
                .accessToken(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .user(UserResponse.from(user))
                .build();
    }

    // 자체 로그인 = 아이디/비밀번호 검증 후 토큰 발급
    @Transactional
    public LoginResponse localLogin(LocalLoginRequest request) {
        User user = userRepository.findByProviderAndProviderId("LOCAL", request.getLoginId())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .orElseThrow(() -> new InvalidCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다."));

        Map<String, String> tokens = tokenService.getTokens(user.getId());

        return LoginResponse.builder()
                .accessToken(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .user(UserResponse.from(user))
                .build();
    }

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
                        .email(info.getEmail())
                        .userName(info.getUserName())
                        .build()));

        Map<String, String> tokens = tokenService.getTokens(user.getId());

        return LoginResponse.builder()
                .accessToken(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .user(UserResponse.from(user))
                .build();
    }

    // 구글 로그인 = 신규면 가입, 기존이면 로그인 (인가코드 하나로 전부 처리)
    @Transactional
    public LoginResponse googleLogin(String code) {
        String googleAccessToken = googleService.getGoogleAccessToken(code);
        GoogleUserInfo info = googleService.getUserInfo(googleAccessToken);

        User user = userRepository
                .findByProviderAndProviderId("GOOGLE", info.getProviderId())
                .map(existing -> {
                    existing.updateProfile(info.getEmail(), info.getUserName());   // 재로그인 시 정보 갱신
                    return existing;
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .provider("GOOGLE")
                        .providerId(info.getProviderId())
                        .email(info.getEmail())
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

    // 회원 탈퇴: TODO → DAILY_LIST → REFRESH_TOKEN → USER 순으로 연쇄 삭제
    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));

        todoRepository.deleteByDailyList_User_Id(userId);
        dailyListRepository.deleteByUserId(userId);
        refreshTokenRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }
}