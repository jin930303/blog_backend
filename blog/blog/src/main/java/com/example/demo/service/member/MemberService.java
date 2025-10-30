package com.example.demo.service.member;

import com.example.demo.dto.member.kakao.KakaoUserInfoDTO;
import com.example.demo.dto.member.MemberDTO;

public interface MemberService {
    void signup_save(MemberDTO dto);

    boolean checkUsername(String username);

    boolean checkNickname(String nickname);

    String getNicknameByUsername(String username);

    String socialLoginOrSignupAndGetJwt(KakaoUserInfoDTO userInfo);

    MemberDTO getMemberInfoById(Long memberId);
}
