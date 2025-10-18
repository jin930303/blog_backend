package com.example.demo.service.member;

import com.example.demo.dto.member.MemberDTO;

public interface MemberService {
    void signup_save(MemberDTO dto);

    boolean checkUsername(String username);

    boolean checkNickname(String nickname);
}
