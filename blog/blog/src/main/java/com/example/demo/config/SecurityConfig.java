package com.example.demo.config;

import com.example.demo.service.member.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. CORS 설정 Bean
    @Bean
    @Order(0)
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 출처 목록 (프론트엔드 주소들)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
       configuration.setAllowedHeaders(Arrays.asList(
               "Authorization",
               "Content-Type",
               "X-Requested-With",
               "Cookie"
       ));
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/**", configuration);
        source.registerCorsConfiguration("/login/oauth2/**",configuration);
        return source;
    }

    // 2. Security Filter Chain 설정
    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter, RateLimitingFilter rateLimitingFilter) throws Exception {
        http
                // 기본 설정 (CORS, CSRF, 세션 등)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. API 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // ========================================================
                        // [GROUP 1] 정적 자원 및 문서 (누구나 접근 가능)
                        // ========================================================
                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**", "/index.html", "/",
                                "/images/**", "/upload/**", "/board.html","/error"
                        ).permitAll()

                        // ========================================================
                        // [GROUP 2] 회원가입, 로그인, OAuth (누구나 접근 가능)
                        // ========================================================
                        .requestMatchers(
                                "/api/v1/login",
                                "/api/v1/member",          // 회원가입
                                "/api/v1/check/**",        // 중복 체크
                                "/login/oauth2/code/kakao",// 카카오 리다이렉트
                                "/api/v1/oauth/kakao/url",  // 카카오 인증 URL
                                "/api/v1/oauth/google/url", // 구글 인증 URL
                                "/login/oauth2/code/google" // 구글 리다이렉트

                        ).permitAll()

                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/v1/notifications/subscribe").permitAll()

                        // ========================================================
                        // [GROUP 3] 인증이 '필수'인 기능 (Authenticated) - 먼저 선언!
                        // ========================================================
                        .requestMatchers("/api/v1/boards/admin/**").permitAll()
                        // 3-1. 게시글 쓰기/수정/삭제/이미지업로드/좋아요
                        .requestMatchers(HttpMethod.POST, "/api/v1/boards").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/boards/upload-image").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/boards/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/boards/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/boards/*/like").authenticated() // 게시글 좋아요

                        // 3-2. 댓글 및 대댓글 (작성/수정/삭제/좋아요)
                        .requestMatchers(HttpMethod.POST, "/api/v1/boards/*/comments").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/boards/*/comments/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/boards/*/comments/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/boards/*/comments/*/like").authenticated() // 댓글 좋아요

                        // 3-3. 알림 및 구독 (쓰기 작업)
                        .requestMatchers("/api/v1/notifications/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/subscriptions/**").authenticated()

                        // 3-4. 테스트용 (필요시 제거)
                        .requestMatchers("/api/v1/test/**").authenticated()

                        // 3-54 마이페이지
                        .requestMatchers("/api/v1/mypage","/api/v1/mypage/*").authenticated()

                        // ========================================================
                        // [GROUP 4] 조회 기능 (누구나 접근 가능) - 나중에 선언!
                        // ========================================================
                        // 위에서 POST, PUT, DELETE 등은 이미 authenticated()로 걸러졌으므로
                        // 여기서 boards/** 를 permitAll 해도 안전하게 GET 요청만 통과되는 효과를 봅니다.
                        .requestMatchers(HttpMethod.GET, "/api/v1/boards/**").permitAll() // 게시글 목록/상세 조회
                        .requestMatchers("/api/v1/boards/search").permitAll()             // 검색
                        .requestMatchers("/api/v1/boards/cursor").permitAll()             // 커서 페이징
                        .requestMatchers(HttpMethod.POST, "/api/v1/boards/markdown-preview").permitAll() // 마크다운 미리보기
                        .requestMatchers(HttpMethod.GET, "/api/v1/subscriptions/**").permitAll() // 구독 여부 조회

                        // ========================================================
                        // [GROUP 5] 그 외 모든 요청
                        // ========================================================

                        .anyRequest().authenticated()
                )

                // JWT 필터 추가
                // * 필터 순서 : RateLimiting -> JWT 순으로 적용
                // RateLimiting이 먼저 차단해야 JWT 파싱 비용도 절약
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter,RateLimitingFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}