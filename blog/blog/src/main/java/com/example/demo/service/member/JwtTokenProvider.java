package com.example.demo.service.member;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

import java.security.Key;

@Component
public class JwtTokenProvider {
    // application.프로펄티 or @벨류를 통해 Secret Key와 만료 설정
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String createToken(String username, String role, String nickname) {
        // 1. 클레임 (정보) 설정: 주체(Subject)와 역할(Role) 등을 담음
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);
        claims.put("nickname", nickname);

        Date now = new Date();
        // 2. 만료 시간 설정 (예: 30분)
        long tokenValidTime = 30 * 60 * 1000L;
        Date validity = new Date(now.getTime() + tokenValidTime);

        // 3. 토큰 생성 후 반환
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
