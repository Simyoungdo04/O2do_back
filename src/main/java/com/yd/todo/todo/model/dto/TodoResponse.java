package com.yd.todo.todo.model.dto;

import com.yd.todo.todo.model.entity.Todo;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoResponse {

    private Long id;
    private String title;
    private String memo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean done;
    private Long originTodoId;
    private int carryOverDays;   // 같은 origin을 가진 TODO 개수 = "며칠째 이월 중인지"

    public static TodoResponse from(Todo todo, int carryOverDays) {
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .memo(todo.getMemo())
                .startTime(todo.getStartTime())
                .endTime(todo.getEndTime())
                .done(todo.isDone())
                .originTodoId(todo.getOriginTodoId())
                .carryOverDays(carryOverDays)
                .build();
    }
}