package com.example.demo.service.member;



import com.example.demo.dto.member.google.GoogleOAuthProperties;
import com.example.demo.dto.member.google.GoogleUserInfoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthService {

    private final GoogleOAuthProperties googleOAuthProperties;
    private final WebClient.Builder webClientBuilder;
    private final MemberService memberService;

    //구글 로그인 페이지 URL 생성
    public String getGoogleAuthUrl(){
        String url ="https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + googleOAuthProperties.getClientId()
                + "&redirect_uri=" + googleOAuthProperties.getRedirectUri()
                + "&response_type=code"
                + "&scope=openid%20profile%20email";
        log.debug("[Google] 생성된 인증 URL: {}", url);
        log.debug("[Google] clientId : {}",googleOAuthProperties.getClientId());
        log.debug("[google] redirectUri : {}",googleOAuthProperties.getRedirectUri());
        return url;
    }

    // 인가 코드 -> 엑세스 토큰
    public String getAccessToken(String code){
        log.debug("[Google] clientSecret 앞 5자리: {}",
                googleOAuthProperties.getClientSecret() );
        WebClient webClient = webClientBuilder.baseUrl("https://oauth2.googleapis.com").build();

        JsonNode response = webClient.post()
                .uri("/token")
                .header("Content-type","application/x-www-form-urlencoded")
                .bodyValue("grant_type=authorization_code"
                + "&client_id="+ googleOAuthProperties.getClientId()
                + "&client_secret="+ googleOAuthProperties.getClientSecret()
                + "&redirect_uri="+ googleOAuthProperties.getRedirectUri()
                + "&code="+code)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if(response == null || response.has("error")){
            throw new RuntimeException("구글 토큰 발급 실패: "+ (response != null ? response.get("error_description").asText() : "응답없음"));

        }

        return response.get("access_token").asText();
    }

    // 엑세스 토큰 -> 사용자 정보
    public GoogleUserInfoDTO getGoogleUserInfo(String accessToken){

        WebClient webClient = webClientBuilder.baseUrl("https://www.googleapis.com").build();

        JsonNode userinfo = webClient.get()
                .uri("/oauth2/v3/userinfo")
                .header("Authorization","Bearer "+accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if(userinfo == null || userinfo.has("error")){
            throw new RuntimeException("구글 사용자 정보 조회 실패");
        }

        return GoogleUserInfoDTO.builder()
                .id(userinfo.get("sub").asText())
                .email(userinfo.get("email").asText())
                .nickname(userinfo.get("name").asText())
                .picture(userinfo.get("picture").asText(null))
                .build();
    }

    public String processLoginAndGetRedirectUrl(String code){
        String accessToken = getAccessToken(code);
        GoogleUserInfoDTO userinfo = getGoogleUserInfo(accessToken);
        String jwtToken = memberService.googleLoginOrSignupAndGetJwt(userinfo);

        String encodedNickname = URLEncoder.encode(
                userinfo.getNickname(), StandardCharsets.UTF_8);

        return "http://localhost:5173"
                + "?token=" + jwtToken
                + "&nickname=" +encodedNickname;
    }

}
