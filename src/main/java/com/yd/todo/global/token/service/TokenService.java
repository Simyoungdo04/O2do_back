package com.yd.todo.global.token.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.todo.global.exception.token.InvalidRefreshTokenException;
import com.yd.todo.global.exception.token.RefreshTokenExpiredException;
import com.yd.todo.global.token.entity.RefreshToken;
import com.yd.todo.global.token.repository.RefreshTokenRepository;
import com.yd.todo.global.token.util.JwtUtil;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private static final long REFRESH_TOKEN_TTL_MILLIS = 1000L * 60 * 60 * 24 * 3;

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public Map<String, String> getTokens(Long userId) {
        Map<String, String> tokens = createTokens(userId);
        saveToken(tokens.get("refreshToken"), userId);
        return tokens;
    }

    private Map<String, String> createTokens(Long userId) {
        return Map.of(
                "accessToken", jwtUtil.getAccessToken(userId),
                "refreshToken", jwtUtil.getRefreshToken(userId)
        );
    }

    private void saveToken(String token, Long userId) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expireDate(System.currentTimeMillis() + REFRESH_TOKEN_TTL_MILLIS)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void logout(String refreshToken, Long userId) {
        log.info("logout refreshToken={}", refreshToken);
        refreshTokenRepository.deleteByUserIdAndToken(userId, refreshToken);
    }

    @Transactional
    public Map<String, String> tokenRotation(String refreshToken) {
        RefreshToken saved = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("일치하는 토큰이 없습니다."));

        if (saved.getExpireDate() < System.currentTimeMillis()) {
            refreshTokenRepository.deleteByUserIdAndToken(saved.getUserId(), refreshToken);
            throw new RefreshTokenExpiredException("리프레시 토큰이 만료되었습니다.");
        }

        Claims claims = jwtUtil.parseJwt(saved.getToken());
        Long userId = Long.valueOf(claims.getSubject());

        Map<String, String> tokens = createTokens(userId);
        refreshTokenRepository.deleteByUserIdAndToken(userId, refreshToken); 
        saveToken(tokens.get("refreshToken"), userId);                       

        return tokens;
    }
}