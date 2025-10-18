package com.example.demo.dto.member;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MemberDTO {
    private long memberId;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String role;
    private String provider;
    private String providerId;
}
