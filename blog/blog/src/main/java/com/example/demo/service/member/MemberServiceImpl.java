package com.example.demo.service.member;

import com.example.demo.dto.member.kakao.KakaoUserInfoDTO;
import com.example.demo.dto.member.MemberDTO;
import com.example.demo.entity.member.MemberEntity;
import com.example.demo.repository.member.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class MemberServiceImpl implements MemberService{

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberServiceImpl(PasswordEncoder passwordEncoder, MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void signup_save(MemberDTO dto) {
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        MemberEntity entity = new MemberEntity();
        entity.setUsername(dto.getUsername());
        entity.setPassword(encodedPassword);
        entity.setNickname(dto.getNickname());
        entity.setEmail(dto.getEmail());
        entity.setRole("role_user");

        memberRepository.save(entity);
    }

    @Override
    public boolean checkUsername(String username) {
        return memberRepository.countByUsername(username) > 0;
    }

    @Override
    public boolean checkNickname(String nickname) {
        return memberRepository.countByNickname(nickname) > 0;
    }

    @Override
    public String getNicknameByUsername(String username) {
        return memberRepository.findNicknameByUsername(username).orElse("사용자");
    }

    // 카카오 로그인 저장
    @Override
    @Transactional // DB 변경이 발생하므로 트랜잭션 처리
    public String socialLoginOrSignupAndGetJwt(KakaoUserInfoDTO userInfo) {
        // 1. providerId(카카오 id)로 기존 회원 찾기
        String providerIdString = userInfo.getId().toString();
        Optional<MemberEntity> memberOptional = memberRepository.findByProviderId(providerIdString);

        MemberEntity entity;
        // providerId가 0개 or 1개일 때 안전하게 작동함
        if(memberOptional.isPresent()) {
            // 2. 기존 회원: 로그인 처리
            entity = memberOptional.get();
        } else {
            // 3. 신규 회원: 회원가입 처리 (DB 저장)
            entity = new MemberEntity();
            entity.setProvider("KAKAO");
            entity.setProviderId(userInfo.getId().toString());
            entity.setUsername("kakao_" + userInfo.getId());
            entity.setNickname(userInfo.getNickname());
            entity.setEmail(userInfo.getEmail());
            // pw 기본이 null 허용 x라 임시 비밀번호를 SOCIAL_providerId로
            String tempPassword = "SOCIAL_" + userInfo.getId();
            entity.setPassword(passwordEncoder.encode(tempPassword));
            entity.setRole("role_user");

            entity = memberRepository.save(entity);
        }

        // 4. JWT 토큰 발행
        return jwtTokenProvider.createToken(entity.getUsername(), entity.getRole(), entity.getNickname());
    }
}
