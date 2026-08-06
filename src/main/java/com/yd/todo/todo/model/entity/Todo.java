package com.yd.todo.todo.model.entity;

import com.yd.todo.dailyList.model.entity.DailyList;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TODO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    private DailyList dailyList;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String memo;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "is_done", nullable = false)
    private boolean done;

    @Column(name = "origin_todo_id")
    private Long originTodoId;   // 최초 원본 TODO id (이월 추적용, 원본이면 자기 자신의 id)

    @Column(name = "postponed_todo_id")
    private Long postponedTodoId;   // 내일로 미룬 경우 그 복제본의 id (미루지 않았으면 null)

    @Column(name = "carried_todo_id")
    private Long carriedTodoId;   // 오늘로 당겨온 경우 그 복제본의 id (당겨오지 않았으면 null)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Todo(DailyList dailyList, String title, String memo,
                LocalDateTime startTime, LocalDateTime endTime, Long originTodoId) {
        this.dailyList = dailyList;
        this.title = title;
        this.memo = memo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.done = false;
        this.originTodoId = originTodoId;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ---------- 도메인 메서드 ----------

    public void update(String title, String memo, LocalDateTime startTime, LocalDateTime endTime) {
        this.title = title;
        this.memo = memo;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void toggleDone() {
        this.done = !this.done;
    }

    // 저장 직후 호출: 원본이면 자기 id를 origin으로 채워서 "이월 계보의 시작점"을 고정
    public void assignOriginIfRoot() {
        if (this.originTodoId == null) {
            this.originTodoId = this.id;
        }
    }

    // 내일로 미룸: 복제본의 id를 기록해두고, 이 TODO는 '오늘의 할 일'에서 제외됨
    public void postpone(Long postponedTodoId) {
        this.postponedTodoId = postponedTodoId;
    }

    public void cancelPostpone() {
        this.postponedTodoId = null;
    }

    public boolean isPostponed() {
        return this.postponedTodoId != null;
    }

    // 오늘(또는 다른 날짜)로 당겨옴: 복제본의 id를 기록해서 이 TODO는 '밀린 할 일'에서 제외됨
    public void markCarried(Long carriedTodoId) {
        this.carriedTodoId = carriedTodoId;
    }

    public boolean isCarried() {
        return this.carriedTodoId != null;
    }

    public void cancelCarry() {
        this.carriedTodoId = null;
    }

    // 이 TODO가 특정 유저 소유인지 확인 (컨트롤러/서비스에서 권한 체크용)
    public boolean isOwnedBy(Long userId) {
        return this.dailyList.getUser().getId().equals(userId);
    }
}