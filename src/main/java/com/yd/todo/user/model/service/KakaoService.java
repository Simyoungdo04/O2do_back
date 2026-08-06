package com.yd.todo.user.model.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.yd.todo.user.model.vo.KakaoTokenResponse;
import com.yd.todo.user.model.vo.KakaoUserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {

	private final RestClient restClient = RestClient.create();
	
	@Value("${kakao.client-secret}")
	private String clientSecret;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    // 인가코드 → 카카오 액세스 토큰 발급
    public String getKakaoAccessToken(String code) {
    	log.info("[KAKAO DEBUG] client_id=[{}] redirect_uri=[{}] code=[{}]", clientId, redirectUri, code);
    	KakaoTokenResponse response = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .body("grant_type=authorization_code"
                        + "&client_id=" + clientId
                        + "&client_secret=" + clientSecret   // ← 추가
                        + "&redirect_uri=" + redirectUri
                        + "&code=" + code)
                .retrieve()
                .body(KakaoTokenResponse.class);

        return response.getAccessToken();
    }

    // 카카오 액세스 토큰 → 사용자 정보 조회
    public KakaoUserInfo getUserInfo(String kakaoAccessToken) {
        return restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + kakaoAccessToken)
                .retrieve()
                .body(KakaoUserInfo.class);
    }
}