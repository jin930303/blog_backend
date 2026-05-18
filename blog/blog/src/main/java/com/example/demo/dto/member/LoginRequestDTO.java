package com.example.demo.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequestDTO {
    @NotBlank(message = "아이디는 필수입니다.")
    @Size(max = 50)
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(max = 100,message ="비밀번호는 100자를 초과할 수 없습니다." )
    private String password;
}
