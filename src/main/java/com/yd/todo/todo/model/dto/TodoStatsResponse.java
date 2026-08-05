package com.yd.todo.todo.model.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoStatsResponse {

    private long completedCount;     // 지금까지 완료한 TODO 총 개수
    private long carryingOverCount;  // 오늘 기준 이월 중인 TODO 개수
}
