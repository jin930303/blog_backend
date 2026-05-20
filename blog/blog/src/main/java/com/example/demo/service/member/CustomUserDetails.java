package com.example.demo.service.member;

import com.example.demo.entity.member.MemberEntity;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

//
@Slf4j
@Getter
public class CustomUserDetails implements UserDetails {

    // ⭐ 컨트롤러에서 사용할 getMemberId() 메서드
    @Getter
    private final Long memberId;
    private final String username;
    private final String nickname;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(MemberEntity memberEntity) {
        this.memberId = memberEntity.getMemberId();
        this.username = memberEntity.getUsername();
        this.password = memberEntity.getPassword();
        this.nickname = memberEntity.getNickname();

        // 권한 설정
        String role = memberEntity.getRole();
        if(role == null){
            // Spring Security Role은 'ROLE_' 접두사 사용하는 것이 일반적입니다.
            role = "ROLE_USER";
        } else{
            role = role.toUpperCase();
        }
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        log.info("CustomUserDetails Created - memberId = {}", memberId); // 생성 시점 로그
    }

    // --- UserDetails 인터페이스 필수 구현 메서드 ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // 아래 4개 메서드는 보통 true를 반환하도록 기본 구현합니다.
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}