package com.example.demo.service.member;

import com.example.demo.dto.member.MemberDTO;
import com.example.demo.entity.member.MemberEntity;
import com.example.demo.repository.member.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService{

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public MemberServiceImpl(PasswordEncoder passwordEncoder, MemberRepository memberRepository) {
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
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
}
