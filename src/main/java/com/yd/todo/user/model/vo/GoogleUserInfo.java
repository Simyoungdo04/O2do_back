package com.yd.todo.user.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleUserInfo {
    private String sub;
    private String email;
    private String name;

    // --- null 안전 편의 메서드 ---
    public String getProviderId() {
        return sub;
    }

    public String getUserName() {
        return name;
    }
}
