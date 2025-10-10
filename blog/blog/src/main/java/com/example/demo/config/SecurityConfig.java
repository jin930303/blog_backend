package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. **핵심 설정**: API 경로는 인증 없이 접근 가능하도록 허용 (API 공개)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/**").permitAll() // /api/v1/로 시작하는 모든 요청 허용
                        // Swagger UI 및 정적 파일 경로도 허용 (필요할 경우)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/index.html", "/").permitAll()
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요 (나머지 페이지 보호)
                )

                // 2. REST API를 위한 설정: CSRF 토큰 비활성화 (프론트엔드 분리 시 필요)
                .csrf(AbstractHttpConfigurer::disable)

                // 3. API 서버이므로 기본 로그인 페이지와 HTTP Basic 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // 4. **HTTP Basic 인증 비활성화**: 팝업 창이 뜨는 것을 확실히 방지
                .httpBasic(AbstractHttpConfigurer::disable)

                // 5. **세션 관리 설정 추가**: REST API 서버는 세션을 사용하지 않으므로 Stateless 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
