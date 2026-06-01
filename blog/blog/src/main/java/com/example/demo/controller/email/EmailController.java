package com.example.demo.controller.email;

import com.example.demo.service.email.EmailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    //인증 코드 발송
    /**
     * POST /api/v1/email/send-verification
     */
    @PostMapping("/send-verification")
    public ResponseEntity<Map<String ,String>> sendVerification(
            @Valid @RequestBody EmailRequest request) {
        emailService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "인증 코드가 발송됐습니다."));
    }


    // 인증 코드 확인
    @PostMapping("/verify")
    public ResponseEntity<Map<String , Object>> verify(
            @Valid @RequestBody VerifyRequest request){
    boolean verified = emailService.verifyCode(request.getEmail(),request.getCode());
    if(!verified){
        return ResponseEntity.badRequest().body(Map.of("verified",false,"message","인증 코드가 올바르지 않거나 만료되었습니다."));
    }
    return ResponseEntity.ok(Map.of("verified",true,"message","인증이 완료되었습니다."));
    }


    // 아이디 찾기
    /**
    * POST /api/v1/email/find-username
     */
    @PostMapping("/find-username")
    public ResponseEntity<Map<String, String>> findUsername(
            @Valid @RequestBody EmailRequest request){
                emailService.sendUsername(request.getEmail());
                return ResponseEntity.ok(Map.of("message","가입하신 이메일로 아이디가 발송되었습니다."));
    }

    // 요청 DTO
    @Getter
    @NoArgsConstructor
    static class EmailRequest{
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;
    }

    @Getter
    @NoArgsConstructor
    static class VerifyRequest{
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @NotBlank(message = "인증 코드를 입력해주세요")
        private String code;

    }

}
