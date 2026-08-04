package com.yd.todo.dailyList.model.dto;

import com.yd.todo.dailyList.model.entity.DailyList;
import com.yd.todo.todo.model.dto.TodoResponse;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyListWithTodosResponse {

    private Long id;
    private LocalDate listDate;
    private List<TodoResponse> todos;

    public static DailyListWithTodosResponse of(DailyList dailyList, List<TodoResponse> todos) {
        return DailyListWithTodosResponse.builder()
                .id(dailyList.getId())
                .listDate(dailyList.getListDate())
                .todos(todos)
                .build();
    }
}