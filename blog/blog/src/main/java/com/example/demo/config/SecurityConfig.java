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
    // ⭐ 1. CorsConfigurationSource Bean 정의: CORS 설정을 Security Filter에 명시적으로 주입합니다.

    @Bean
    @Order(0)
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 임시 ip 등록
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://172.30.1.9:5500",
                "http://localhost:5500",
                "http://192.168.0.8:5500",
                "http://192.168.0.28:5500",
                "http://127.0.0.1:5500",
                "http://192.168.0.3:5500",
                "http://192.168.0.13:5500"

        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);

        // 경로 "/api/v1/**"에 이 CORS 설정을 적용합니다.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/**", configuration);
        return source;
    }

    @Bean
    @Order(1)// 우선순위를 높이는
    public SecurityFilterChain securityFilterChain(HttpSecurity http,  JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                // CORS 설정을 커스터마이징된 corsConfigurationSource Bean으로 적용
                .cors(cors ->cors.configurationSource(corsConfigurationSource()))
                // 1. **핵심 설정**: API 경로는 인증 없이 접근 가능하도록 허용 (API 공개)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,"/api/v1/subscriptions/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/v1/subscriptions/**").authenticated()
                        .requestMatchers("/api/v1/notifications/subscribe").authenticated()
                        .requestMatchers("/api/v1/test/**").authenticated()
                        .requestMatchers("/api/v1/boards/auth-check").permitAll()
                        .requestMatchers("/api/v1/boards/{boardId}/like").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/v1/boards/markdown-preview").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/v1/boards/{boardId}").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/v1/boards/{boardId}/**").permitAll()
                        .requestMatchers("/api/v1/boards/search").permitAll()
                        // 카카오 로그인 추가하기
                        // 1. 카카오 Redirect URI (가장 중요한 부분)
                        .requestMatchers("/login/oauth2/code/kakao").permitAll()
                        // 2. 카카오 인증 시작 URL 요청 엔드포인트
                        .requestMatchers("/api/v1/oauth/kakao/url").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/upload/**").permitAll()
                        .requestMatchers("/upload/images/**").permitAll()
                        // Swagger UI 및 정적 파일 경로도 허용 (필요할 경우)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/index.html", "/").permitAll()
                        .requestMatchers("/board.html").permitAll()
                        .requestMatchers("/upload/images/**").permitAll()
                        .requestMatchers("/api/v1/boards/cursor").permitAll()
                        .requestMatchers("/api/v1/boards").permitAll()
                        .requestMatchers("/api/v1/login", "/api/v1/member", "/api/v1/check/**").permitAll()
                        // /api/v1/로 시작하는 모든 요청 허용
                        // 이건 토큰 없이도 통과가 되는데 그래서 필터 돌아도 참조 안 해서 null일 수도 있음
//                        .requestMatchers("/api/v1/**").permitAll()
                        .requestMatchers("/api/v1/boards/upload-image").authenticated()


                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요 (나머지 페이지 보호)
                )

                // 2. REST API를 위한 설정: CSRF 토큰 비활성화 (프론트엔드 분리 시 필요)
                .csrf(AbstractHttpConfigurer::disable)

                // 3. API 서버이므로 기본 로그인 페이지와 HTTP Basic 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // 4. **HTTP Basic 인증 비활성화**: 팝업 창이 뜨는 것을 확실히 방지
                .httpBasic(AbstractHttpConfigurer::disable)

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // 5. **세션 관리 설정 추가**: REST API 서버는 세션을 사용하지 않으므로 Stateless 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

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
