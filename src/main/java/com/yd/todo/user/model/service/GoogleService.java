package com.yd.todo.user.model.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.yd.todo.user.model.vo.GoogleTokenResponse;
import com.yd.todo.user.model.vo.GoogleUserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleService {

	private final RestClient restClient = RestClient.create();

	@Value("${google.client-id}")
	private String clientId;

	@Value("${google.client-secret}")
	private String clientSecret;

	@Value("${google.redirect-uri}")
	private String redirectUri;

    // 인가코드 → 구글 액세스 토큰 발급
    public String getGoogleAccessToken(String code) {
    	GoogleTokenResponse response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .body("grant_type=authorization_code"
                        + "&client_id=" + clientId
                        + "&client_secret=" + clientSecret
                        + "&redirect_uri=" + redirectUri
                        + "&code=" + code)
                .retrieve()
                .body(GoogleTokenResponse.class);

        return response.getAccessToken();
    }

    // 구글 액세스 토큰 → 사용자 정보 조회
    public GoogleUserInfo getUserInfo(String googleAccessToken) {
        return restClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + googleAccessToken)
                .retrieve()
                .body(GoogleUserInfo.class);
    }
}
