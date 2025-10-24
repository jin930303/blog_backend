package com.example.demo.service.member;

import com.example.demo.entity.member.MemberEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Collections;

//
@Getter
public class CustomUserDetails extends User {

    private final Long memberId;

    public CustomUserDetails(MemberEntity memberEntity, Collection<? extends GrantedAuthority> authorities) {
        super(memberEntity.getUsername(), memberEntity.getPassword(), authorities);
        this.memberId = memberEntity.getMemberId();
    }

    public CustomUserDetails(MemberEntity memberEntity) {
        this(memberEntity, Collections.singletonList(new SimpleGrantedAuthority(memberEntity.getRole())));
    }
}
