package com.example.demo.dto.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordResetRequestDTO {

    @NotBlank(message = "아이디를 입력해주세요")
    private String username;

    @NotBlank(message = "이메일을 입력해주세요")
    private String email;
}
