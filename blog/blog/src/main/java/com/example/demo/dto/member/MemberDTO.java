package com.example.demo.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class MemberDTO {
    private long memberId;
    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4,max = 20,message = "아이디는 4자 이상 12자 이하입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$",message = "아이디는 영문, 숫자만 가능합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8,max = 100, message ="비밀번호는 8자 이상, 100자 이하입니다." )
    @Pattern(regexp ="^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$",message ="비밀번호는 대문자와 특수문자를 각각 1개 이상 포함해야 합니다." )
    private String password;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2,max = 20,message = "닉네임은 2자 이상 20자 이하입니다.")
    private String nickname;

    @Size(max = 100,message = "이메일은 100자를 초과할 수 없습니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Builder.Default
    private String role = "ROLE_USER";
    private String provider;
    private String providerId;

}
