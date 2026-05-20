package com.example.demo.service.member;

import com.example.demo.dto.member.kakao.KakaoOAuthProperties;
import com.example.demo.dto.member.kakao.KakaoUserInfoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class KakaoOAuthService {
    private final KakaoOAuthProperties kakaoProperties;
    private final WebClient webClient;

    public KakaoOAuthService(KakaoOAuthProperties kakaoProperties, WebClient.Builder webClientBuilder) {
        this.kakaoProperties = kakaoProperties;
        this.webClient = webClientBuilder.baseUrl("").build();
    }

    // VS Code에 전달할 카카오 인증 요청 URL 생성
    public String getKakaoAuthUrl() {
        return "https://kauth.kakao.com/oauth/authorize" +
                "?client_id=" + kakaoProperties.getClientId() + // 설정 클래스 사용
                "&redirect_uri=" + kakaoProperties.getRedirectUri() + // 설정 클래스 사용
                "&response_type=code";
    }

    // 인가 코드(code)를 사용해 카카오한테 액세스 토큰 발급받기
    public String getAccessToken(String code) {
//        // 🚨 실제 구현에서는 에러 처리 로직이 추가되어야 합니다.

        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("grant_type","authorization_code");
        params.add("client_id",kakaoProperties.getClientId());
        params.add("redirect_uri",kakaoProperties.getRedirectUri());
        params.add("code",code);
        JsonNode responseNode = webClient.post()
                .uri(kakaoProperties.getTokenUri())
                .header("Content-Type", "application/x-www-form-urlencoded")
//                .bodyValue("grant_type=authorization_code" +
//                        "&client_id=" + kakaoProperties.getClientId() +
//                        "&redirect_uri=" + kakaoProperties.getRedirectUri() +
//                        "&code=" + code)
                .bodyValue(params)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse -> clientResponse.bodyToMono(String.class)
                        .doOnNext(body -> System.out.println("카카오 에러 응답: "+body))
                        .then(Mono.error(new RuntimeException("카카오 400 에러"))))
                .bodyToMono(JsonNode.class)
                .block(); // 블로킹 방식으로 응답 대기

        if (responseNode == null || responseNode.has("error")) {
            throw new RuntimeException("카카오 토큰 발급 실패: " + (responseNode != null ? responseNode.get("error_description").asText() : "응답 없음"));
        }
        return responseNode.get("access_token").asText();
    }

    /**
     * 액세스 토큰을 사용하여 카카오 사용자 정보를 조회합니다.
     * @param accessToken 발급받은 액세스 토큰
     * @return 사용자 정보를 담은 JsonNode
     */
    public KakaoUserInfoDTO getKakaoUserInfo(String accessToken) { // ⭐️ 반환 타입을 KakaoUserInfoDto로 변경
        JsonNode userInfo = webClient.get()
                .uri(kakaoProperties.getUserInfoUri())
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (userInfo == null || userInfo.has("error")) {
            throw new RuntimeException("카카오 사용자 정보 조회 실패");
        }

        // 필수 항목 파싱
        Long id = userInfo.get("id").asLong();
        JsonNode properties = userInfo.get("properties");
        String nickname = properties.get("nickname").asText();
        String profileImageUrl = properties.get("profile_image").asText();

        // 선택 항목 (이메일) 파싱
        String email = null;
        if (userInfo.has("kakao_account")) {
            JsonNode kakaoAccount = userInfo.get("kakao_account");
            // 이메일 동의 항목에 따라 'email' 필드의 존재 여부가 달라집니다.
//            if (kakaoAccount.has("email_needs_agreement") && kakaoAccount.get("email_needs_agreement").asBoolean() == false) {
//                email = kakaoAccount.get("email").asText();
//            }
            if(kakaoAccount.has("email")){
                email = kakaoAccount.get("email").asText();
            }

        }
        if(email == null){
            email = "kakao_"+id+"@kakao.com";
        }

        // ⭐️ KakaoUserInfoDto 객체를 생성하여 반환합니다.
        return KakaoUserInfoDTO.builder()
                .id(id)
                .nickname(nickname)
                .profile_image(profileImageUrl)
                .email(email)
                .build();
    }
}
