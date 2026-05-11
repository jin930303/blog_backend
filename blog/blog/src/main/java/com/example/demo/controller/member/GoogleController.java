package com.example.demo.controller.member;

import com.example.demo.service.member.GoogleOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GoogleController {

    private final GoogleOAuthService googleOAuthService;

    @GetMapping("/api/v1/oauth/google/url")
    public ResponseEntity<Map<String,String>> getGoogleAuthUrl(){
        return ResponseEntity.ok(
                Map.of("googleAuthUrl", googleOAuthService.getGoogleAuthUrl())
        );
    }

    @GetMapping("/login/oauth2/code/google")
    public ResponseEntity<?> googleCallback(@RequestParam("code") String code){
        try{
            String redirectUrl = googleOAuthService.processLoginAndGetRedirectUrl(code);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION,redirectUrl)
                    .build();
        } catch (Exception e){
            log.error("[Google OAuth] 로그인 처리 중 오류 발생 -message: {}",e.getMessage(),e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error",e.getMessage()));
        }

    }

}
