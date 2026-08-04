package com.yd.todo.dailyList.controller;

import com.yd.todo.dailyList.model.dto.DailyListResponse;
import com.yd.todo.dailyList.model.dto.DailyListWithTodosResponse;
import com.yd.todo.dailyList.model.entity.DailyList;
import com.yd.todo.dailyList.model.service.DailyListService;
import com.yd.todo.global.auth.LoginUser;
import com.yd.todo.global.common.ApiResponse;
import com.yd.todo.todo.model.dto.TodoResponse;
import com.yd.todo.todo.model.service.TodoService;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dailylist")
@RequiredArgsConstructor
public class DailyListController {

    private final DailyListService dailyListService;
    private final TodoService todoService;

    // date 없으면 오늘 리스트 + TODO 전체, date 있으면 해당 날짜 리스트만 (지난 기록은 TODO 없이 우선 제공)
    @GetMapping
    public ResponseEntity<ApiResponse<DailyListWithTodosResponse>> getDailyList(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(value = "date", required = false) LocalDate date) {

        if (date == null) {
            DailyListWithTodosResponse response = todoService.findTodayDailyList(loginUser.getUserId());
            return ResponseEntity.ok(ApiResponse.success(200, "조회 성공", response));
        }

        DailyList dailyList = dailyListService.getByDate(loginUser.getUserId(), date);
        List<TodoResponse> todos = todoService.findTodosByDailyListId(dailyList.getId());
        DailyListWithTodosResponse response = DailyListWithTodosResponse.of(dailyList, todos);
        return ResponseEntity.ok(ApiResponse.success(200, "조회 성공", response));
    }
}