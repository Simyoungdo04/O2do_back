package com.yd.todo.global.token.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.yd.todo.global.token.model.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Transactional
    void deleteByUserIdAndToken(Long userId, String token);

    // 회원 탈퇴 시 해당 유저의 REFRESH_TOKEN 전체 삭제
    @Transactional
    void deleteByUserId(Long userId);
}