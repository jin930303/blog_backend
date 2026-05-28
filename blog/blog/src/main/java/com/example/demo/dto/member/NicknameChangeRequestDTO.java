package com.example.demo.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NicknameChangeRequestDTO {

    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(min = 2,max = 20,message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    private String nickname;
}
