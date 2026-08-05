package com.yd.todo.todo.controller;

import com.yd.todo.dailyList.model.dto.DailyListWithTodosResponse;
import com.yd.todo.global.auth.LoginUser;
import com.yd.todo.global.common.ApiResponse;
import com.yd.todo.todo.model.dto.TodoCreateRequest;
import com.yd.todo.todo.model.dto.TodoResponse;
import com.yd.todo.todo.model.dto.TodoStatsResponse;
import com.yd.todo.todo.model.dto.TodoUpdateRequest;
import com.yd.todo.todo.model.service.TodoService;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    // 오늘의 할 일 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<TodoResponse>>> findTodayTodos(
            @AuthenticationPrincipal LoginUser loginUser) {

        List<TodoResponse> response = todoService.findTodayTodos(loginUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(200, "조회 성공", response));
    }

    // 사이드바 통계: 총 완료 개수 + 오늘 기준 이월 중인 개수
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<TodoStatsResponse>> stats(
            @AuthenticationPrincipal LoginUser loginUser) {

        TodoStatsResponse response = todoService.getStats(loginUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(200, "통계 조회 성공", response));
    }

    // 오늘 이전 날짜의 미완료 TODO를 날짜별로 모아서 조회
    @GetMapping("/incomplete-history")
    public ResponseEntity<ApiResponse<List<DailyListWithTodosResponse>>> incompleteHistory(
            @AuthenticationPrincipal LoginUser loginUser) {

        List<DailyListWithTodosResponse> response = todoService.getIncompleteHistory(loginUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(200, "미완료 기록 조회 성공", response));
    }

    // TODO 생성
    @PostMapping
    public ResponseEntity<ApiResponse<TodoResponse>> create(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody TodoCreateRequest request) {

        TodoResponse response = todoService.create(loginUser.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(200, "등록 성공", response));
    }

    // TODO 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoResponse>> update(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable("id") Long id,
            @Valid @RequestBody TodoUpdateRequest request) {

        TodoResponse response = todoService.update(loginUser.getUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(200, "수정 성공", response));
    }

    // 완료 상태 토글
    @PatchMapping("/{id}/done")
    public ResponseEntity<ApiResponse<TodoResponse>> toggleDone(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable("id") Long id) {

        TodoResponse response = todoService.toggleDone(loginUser.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(200, "상태 변경 성공", response));
    }

    // 수동 이월: 과거 미완료 TODO를 오늘로 복제
    @PostMapping("/{id}/carry")
    public ResponseEntity<ApiResponse<TodoResponse>> carryOver(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable("id") Long id) {

        TodoResponse response = todoService.carryOver(loginUser.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(200, "이월 성공", response));
    }

    // 오늘의 미완료 TODO를 내일로 미루기
    @PostMapping("/{id}/postpone")
    public ResponseEntity<ApiResponse<TodoResponse>> postpone(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable("id") Long id) {

        TodoResponse response = todoService.postponeToTomorrow(loginUser.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(200, "내일로 미루기 성공", response));
    }

    // 내일로 미루기 취소
    @DeleteMapping("/{id}/postpone")
    public ResponseEntity<ApiResponse<Void>> cancelPostpone(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable("id") Long id) {

        todoService.cancelPostpone(loginUser.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(200, "미루기 취소 성공", null));
    }

    // TODO 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable("id") Long id) {

        todoService.delete(loginUser.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(200, "삭제 성공", null));
    }
}