package com.example.demo.service.member;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserService customUserService;

    private String resolveTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            // JwtTokenProvider에서 설정한 쿠키 이름인 "accessToken"을 사용합니다.
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰을 추출 (기존 방식 유지 - 혹시 모를 Bearer 토큰 요청 대비)
        String token = null;
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7).trim();
        }

        // 2. Authorization 헤더에 토큰이 없으면 HttpOnly 쿠키에서 토큰을 추출 (⭐ 핵심 수정 ⭐)
        if (token == null) {
            token = resolveTokenFromCookie(request);
        }

        if (token != null) {
            try {
                // 토큰 유효성 검사 및 클레임 파싱
                Claims claims = jwtTokenProvider.parseClaims(token);
                String username = claims.getSubject();

                UserDetails userDetails = customUserService.loadUserByUsername(username);

                if (userDetails != null) {
                    // 인증 객체 생성 및 SecurityContext에 저장
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                // 토큰이 유효하지 않은 경우 (만료, 변조 등)
                logger.warn("JWT 인증 실패: {}" + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);

    }
}
