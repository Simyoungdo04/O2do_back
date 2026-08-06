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
    private Long postponedTodoId;   // 내일로 미룬 경우 그 복제본의 id (미루지 않았으면 null)
    private Long carriedTodoId;   // 오늘로 당겨온 경우 그 복제본의 id (당겨오지 않았으면 null)
    private boolean effectiveDone;   // 이 TODO 자신 또는 미루기/당겨오기로 이어진 복제본이 최종적으로 완료됐는지

    public static TodoResponse from(Todo todo, int carryOverDays, boolean effectiveDone) {
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .memo(todo.getMemo())
                .startTime(todo.getStartTime())
                .endTime(todo.getEndTime())
                .done(todo.isDone())
                .originTodoId(todo.getOriginTodoId())
                .carryOverDays(carryOverDays)
                .postponedTodoId(todo.getPostponedTodoId())
                .carriedTodoId(todo.getCarriedTodoId())
                .effectiveDone(effectiveDone)
                .build();
    }
}