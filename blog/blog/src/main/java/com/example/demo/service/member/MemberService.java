package com.example.demo.service.member;

import com.example.demo.dto.board.MyBoardResponseDTO;
import com.example.demo.dto.member.NicknameChangeRequestDTO;
import com.example.demo.dto.member.PwChangeRequestDTO;
import com.example.demo.dto.member.google.GoogleUserInfoDTO;
import com.example.demo.dto.member.kakao.KakaoUserInfoDTO;
import com.example.demo.dto.member.MemberDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface MemberService {
    void signup_save(MemberDTO dto);

    boolean checkUsername(String username);

    boolean checkNickname(String nickname);

    String getNicknameByUsername(String username);

    String socialLoginOrSignupAndGetJwt(KakaoUserInfoDTO userInfo);

    MemberDTO getMemberInfoById(Long memberId);

    String googleLoginOrSignupAndGetJwt(GoogleUserInfoDTO userinfo);

    void changePw(Long memberId, @Valid PwChangeRequestDTO dto);

    void changeNickname(@Valid NicknameChangeRequestDTO dto, Long memberId);

    List<MyBoardResponseDTO> getMyBoards(Long memberId);

    void verifyCurrentPassword(Long memberId, String currentPw);
}
