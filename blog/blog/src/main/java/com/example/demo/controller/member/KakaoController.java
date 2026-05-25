package com.example.demo.controller.member;

import com.example.demo.dto.member.kakao.KakaoUserInfoDTO;
import com.example.demo.service.member.JwtTokenProvider;
import com.example.demo.service.member.KakaoOAuthService;
import com.example.demo.service.member.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoOAuthService kakaoOAuthService;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

//    public KakaoController(KakaoOAuthService kakaoOAuthService, MemberService memberService, JwtTokenProvider jwtTokenProvider) {
//        this.kakaoOAuthService = kakaoOAuthService;
//        this.memberService = memberService;
//        this.jwtTokenProvider = jwtTokenProvider;
//    }

    @Value("${fronted.url}")
    private String frontedUrl;

    // 로그인 시작 URL 제공
    @GetMapping("/api/v1/oauth/kakao/url")
    public ResponseEntity<Map<String, String>> getKakaoAuthUrl() {
//        String authUrl = kakaoOAuthService.getKakaoAuthUrl();
//        Map<String, String> response = new HashMap<>();
//        response.put("kakaoAuthUrl", authUrl);
        return ResponseEntity.ok(Map.of("kakaoAuthUrl", kakaoOAuthService.getKakaoAuthUrl()));
    }

    // redirect URI 처리
    /*
        - 인가 코드는 카카오 로그인 성공적으로 마쳤다는 사실을 증명하는 일회용 임시 비밀번호.
        - 카카오 서버가 백엔드 서버로 직접 토큰 주는 대신 코드 먼저 줌
        - 보안, 백엔드 서버는 인가 코드를 액세스 토큰이란 실제 권한으로 교환해야됨.
        - 여기서 인가 코드를 받아서 KakaoOAuthService로 넘겨 토큰 교환하는 데 사용함
        - Security가 코드를 직접 처리는 안 하는데, 이거 요청한 엔드포인트를 막으면 403에러 바로 뜸
    */


    @GetMapping("/login/oauth2/code/kakao")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) {

        try {
            // 1. 인가 코드로 액세스 토큰 발급
            String accessToken = kakaoOAuthService.getAccessToken(code);

            // 2. 액세스 토큰으로 사용자 정보 조회 및 DTO 변환
            KakaoUserInfoDTO userInfo = kakaoOAuthService.getKakaoUserInfo(accessToken);

            /*
             처리 완료 시 임시 데이터 반환
            Map<String, Object> response = new HashMap<>();
            response.put("message", "카카오 로그인 성공 및 정보 획득 완료");
            response.put("providerId", userInfo.getId());
            response.put("nickname", userInfo.getNickname());
            response.put("email", userInfo.getEmail()); // 선택 동의가 되었으면 값이 존재

            return ResponseEntity.ok(response);

             */

            // 3. 사용자 정보(userInfo)를 기반으로 서비스 회원가입/로그인 처리 (TODO: MemberService 호출)
            String jwtToken = memberService.socialLoginOrSignupAndGetJwt(userInfo);
//            String userNickname = userInfo.getNickname(); // 카카오에서 받은 닉네임

            String encodeNickname = URLEncoder.encode(userInfo.getNickname(), StandardCharsets.UTF_8.toString());

            //쿠키에 토큰 심기
            jwtTokenProvider.addTokenToCookie(response,jwtToken);

            // 4. 자체 인증(JWT) 토큰 발급 후 프론트엔드의 메인 페이지로 리다이렉트
            // 물음표는 URL에서 쿼리 문자열이 시작됨을 나타냄
            // token= 은 데이터의 key
            // jwtToken 은 데이터의 Value
            // & 를 사용하여 닉네임 파라미터 추가
            String redirectUrl = frontedUrl
//                    + "?token=" + jwtToken
                    + "?nickname=" + encodeNickname;

            return ResponseEntity.status(HttpStatus.FOUND) // HTTP 302 Found 응답
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();

            // RuntimeException 대신 Exception으로 변경하여 URLEncoder의 IOException 처리
        } catch (Exception e) {
            // 예외 발생 시 (토큰 발급 실패, 정보 조회 실패 등)
            log.error("[Kakao OAuth] 로그인 처리 중 오류 발생 - message: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
