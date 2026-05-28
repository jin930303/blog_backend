package com.example.demo.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PwChangeRequestDTO {

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Size(min = 8,max = 100,message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
    @Pattern(regexp ="^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$",message ="비밀번호는 대문자와 특수문자를 각각 1개 이상 포함해야 합니다." )
    private String newPw;

    @NotBlank(message = "새 비밀번호 확인을 입력해주세요")
    private String newPwConfirm;
}
