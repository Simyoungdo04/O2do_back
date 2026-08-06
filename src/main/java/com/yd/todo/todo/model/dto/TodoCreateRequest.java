package com.yd.todo.todo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodoCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    @Size(max = 1000, message = "메모는 1000자 이하여야 합니다.")
    private String memo;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDate targetDate;   // 미지정 시 오늘 리스트에 생성
}