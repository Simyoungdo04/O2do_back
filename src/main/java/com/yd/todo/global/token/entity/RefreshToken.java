package com.yd.todo.global.token.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "REFRESH_TOKEN")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 500, unique = true)
    private String token;

    @Column(name = "expire_date", nullable = false)
    private Long expireDate;

    @Builder
    public RefreshToken(Long userId, String token, Long expireDate) {
        this.userId = userId;
        this.token = token;
        this.expireDate = expireDate;
    }
}