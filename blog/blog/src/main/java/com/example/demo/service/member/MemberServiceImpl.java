package com.example.demo.service.member;

import com.example.demo.dto.member.MemberDTO;
import com.example.demo.entity.member.MemberEntity;
import com.example.demo.repository.member.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService{
    @Autowired
    MemberRepository memberRepository;

    @Override
    public void signup_save(MemberDTO dto) {
        MemberEntity entity = new MemberEntity();
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword());
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
}
