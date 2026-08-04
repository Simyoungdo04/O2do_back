package com.yd.todo.dailyList.model.entity;

import com.yd.todo.user.model.entity.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "DAILY_LIST",
    uniqueConstraints = @UniqueConstraint(
        name = "UK_DAILY_LIST_USER_DATE",
        columnNames = {"user_id", "list_date"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "list_date", nullable = false)
    private LocalDate listDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public DailyList(User user, LocalDate listDate) {
        this.user = user;
        this.listDate = listDate;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}