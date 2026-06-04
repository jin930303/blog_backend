package com.example.demo.service.member;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;

import java.time.Duration;
import java.util.Date;

import java.security.Key;

@Component
public class JwtTokenProvider {
    // application.프로펄티 or @벨류를 통해 Secret Key와 만료 설정
    @Value("${jwt.secret-key}")
    private String seceretKeyString;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(seceretKeyString.getBytes());
    }

    public String createToken(String username, String role, String nickname) {
        // 1. 클레임 (정보) 설정: 주체(Subject)와 역할(Role) 등을 담음
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);
        claims.put("nickname", nickname);

        Date now = new Date();
        // 2. 만료 시간 설정
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
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(true)          // ngrok은 HTTPS니까 항상 true
                .sameSite("None")      // cross-site 쿠키 허용 (핵심!)
                .path("/")
                .maxAge(Duration.ofMinutes(30))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
