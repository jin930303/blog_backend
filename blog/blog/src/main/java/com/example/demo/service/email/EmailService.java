package com.example.demo.service.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public interface EmailService {
    void sendVerificationCode(@NotBlank(message = "이메일을 입력해주세요.") @Email(message = "이메일 형식이 올바르지 않습니다.") String email);

    boolean verifyCode(@NotBlank(message = "이메일을 입력해주세요") @Email(message = "이메일 형식이 올바르지 않습니다.") String email, @NotBlank(message = "인증 코드를 입력해주세요") String code);

    void sendUsername(@NotBlank(message = "이메일을 입력해주세요.") @Email(message = "이메일 형식이 올바르지 않습니다.") String email);

    boolean isVerified(@Size(max = 100,message = "이메일은 100자를 초과할 수 없습니다.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email);

    void deleteVerified(@Size(max = 100,message = "이메일은 100자를 초과할 수 없습니다.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email);
}
