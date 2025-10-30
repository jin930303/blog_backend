package com.example.demo.service.member;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰 저장 방식 강화하기, JWT를 HttpOnly 쿠키로 관리
    public void addTokenToCookie(HttpServletResponse response, String token) {
        // 1. HttpOnly 쿠키 생성
        Cookie cookie = new Cookie("accessToken", token);

        // 2. 보안 설정 적용하기
        // HttpOnly: JavaScript 접근 불가 -> 보안관련 공격 방어
        cookie.setHttpOnly(true);

        // 쿠키가 전송될 경로 설정(모든 곳으로)
        cookie.setPath("/");

        // 쿠키 만료 시간 30분
        cookie.setMaxAge(30*60);

        // 3. 응답에 쿠키 추가하기
        response.addCookie(cookie);
    }
}
