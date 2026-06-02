package com.example.demo.service.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public interface EmailService {
    void sendVerificationCode(@NotBlank(message = "이메일을 입력해주세요.") @Email(message = "이메일 형식이 올바르지 않습니다.") String email);

    boolean verifyCode(@NotBlank(message = "이메일을 입력해주세요") @Email(message = "이메일 형식이 올바르지 않습니다.") String email, @NotBlank(message = "인증 코드를 입력해주세요") String code);

    void sendUsername(@NotBlank(message = "이메일을 입력해주세요.") @Email(message = "이메일 형식이 올바르지 않습니다.") String email);

    boolean isVerified(@Size(max = 100,message = "이메일은 100자를 초과할 수 없습니다.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email);

    void deleteVerified(@Size(max = 100,message = "이메일은 100자를 초과할 수 없습니다.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email);

    void sendPasswordResetLink(@NotBlank(message = "아이디를 입력해주세요") String username, @NotBlank(message = "이메일을 입력해주세요") String email);

    boolean isValidResetToken(String token);

    void resetPassword(@NotBlank(message = "토큰이 없습니다.") String token, @NotBlank(message = "새 비밀번호를 입력해주세요") @Size(min = 8,max = 100,message = "비밀번호는 8자 이상 100자 이하여야합니다.") @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$",
            message = "비밀번호는 대문자와 특수문자를 각각 1개 이상 포함해야 합니다.") String newPassword);
}
