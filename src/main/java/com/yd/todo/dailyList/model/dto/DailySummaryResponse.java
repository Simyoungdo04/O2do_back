package com.yd.todo.dailyList.model.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailySummaryResponse {
    private LocalDate date;
    private int total;
    private int done;
}
