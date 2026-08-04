package com.yd.todo.global.token.model.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;

    public static TokenResponse from(Map<String, String> tokens) {
        return TokenResponse.builder()
                .accessToken(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .build();
    }
}