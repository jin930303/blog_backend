package com.example.demo.service.member;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Slf4j
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
                String value = cookie.getValue();

                log.debug("accessToken 반환 값: [{}]", value);
                log.debug("accessToken 길이: {}", value != null ? value.length() : 0);
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

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                log.debug("쿠키 확인: name={}, value={}",cookie.getName(),cookie.getValue());
            }
        } else {
            log.warn("요청에 쿠키가 없습니다. URI={}", request.getRequestURI());
        }
        // 1. Authorization 헤더에서 토큰을 추출 (기존 방식 유지 - 혹시 모를 Bearer 토큰 요청 대비)
        String token = null;
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String extracted = header.substring(7).trim();
            if(!extracted.isEmpty()){
                token =extracted;
            }
//            token = header.substring(7).trim();

        }

        // 2. Authorization 헤더에 토큰이 없으면 HttpOnly 쿠키에서 토큰을 추출 (⭐ 핵심 수정 ⭐)
        if (token == null) {
            token = resolveTokenFromCookie(request);
        }

        if (token != null) {
            log.debug("파싱할 토큰 : [{}]",token);
            try {
                // 토큰 유효성 검사 및 클레임 파싱
                Claims claims = jwtTokenProvider.parseClaims(token);
                String username = claims.getSubject();

                UserDetails userDetails = customUserService.loadUserByUsername(username);

                if (userDetails != null) {

                    if(!userDetails.isAccountNonLocked()){
                        log.warn("[AUTH] 차단된 회원 요청 거부 - username={} URI={}",username,request.getRequestURI());
                        sendBlockedResponse(response);
                        return;
                    }

                    // 인증 객체 생성 및 SecurityContext에 저장
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                // 토큰이 유효하지 않은 경우 (만료, 변조 등)
                log.warn("JWT 인증 실패: {}" , e.getMessage());
            }
        }

        filterChain.doFilter(request, response);

    }

    private void sendBlockedResponse(HttpServletResponse response) throws IOException{
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"차단된 계정입니다. 관리자에게 문의하세요.\"}"
        );
    }

}
