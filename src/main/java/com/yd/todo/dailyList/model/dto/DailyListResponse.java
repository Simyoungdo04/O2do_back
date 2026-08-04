package com.yd.todo.dailyList.model.dto;

import com.yd.todo.dailyList.model.entity.DailyList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyListResponse {

    private Long id;
    private LocalDate listDate;
    private LocalDateTime createdAt;

    public static DailyListResponse from(DailyList dailyList) {
        return DailyListResponse.builder()
                .id(dailyList.getId())
                .listDate(dailyList.getListDate())
                .createdAt(dailyList.getCreatedAt())
                .build();
    }
}