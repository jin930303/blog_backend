package com.example.demo.service.member;

import com.example.demo.entity.member.MemberEntity;
import com.example.demo.repository.member.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberEntity memberEntity = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("가입된 정보를 찾을 수 없습니다."));

        String role = memberEntity.getRole();
        if(role == null){
            role = "role_user"; // USER -> role_user
        }

        return new CustomUserDetails(memberEntity);
//        return User.builder()
//                .username(memberEntity.getUsername())
//                .password(memberEntity.getPassword())
//                .roles(role)
//                .build();
    }
}
