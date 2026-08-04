package com.yd.todo.todo.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodoUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    private String memo;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}