package com.yd.todo.user.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "TODO_USER",
    uniqueConstraints = @UniqueConstraint(
        name = "UK_USERS_PROVIDER",
        columnNames = {"provider", "provider_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String provider;            // "KAKAO"

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;          // 카카오가 준 회원 고유 id

    @Column(length = 100)
    private String email;               // 동의 항목 → null 가능

    @Column(name = "user_name", length = 50)
    private String userName;            // 카카오 닉네임 → null 가능

    @Column(length = 100)
    private String password;            // BCrypt 해시, LOCAL 계정만 사용 → 소셜 계정은 null

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Builder
    public User(String provider, String providerId, String email, String userName, String password) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.userName = userName;
        this.password = password;
    }

    @PrePersist
    void prePersist() {
        this.createDate = LocalDateTime.now();
    }

    public void updateProfile(String email, String userName) {
        this.email = email;
        this.userName = userName;
    }
}