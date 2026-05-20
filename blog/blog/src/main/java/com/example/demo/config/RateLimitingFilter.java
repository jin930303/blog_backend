package com.example.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Rate Limiting 필터
 *
 *  전략 :
 *  - 로그인 엔드포인트 (/api/v1/login) ip 당 5회/분 (브루트포스 방어)
 *  - 회원가입 엔드포인트 (/api/v1/member POST): ip 당 3회/분 (스팸 가입 방어)
 *  - 일반 API : ip당 100회/분
 *
 *  버킷 관리 :
 *  - ConcurrentHashMap 으로 ip별 버킷을 인메모리 관리
 *  - 스케줄러로 1시간 이상 미사용 버킷 자동 제거 (메모리 누수 방지)
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter{

    //엔드포인트 상수
    private static final String LOGIN_PATH ="/api/v1/login";
    private static final String REGISTER_PATH = "/api/v1/member";

    //제한 설정
    /** 로그인: 1분안에 최대 5회 */
    private static final int LOGIN_CAPACITY = 5;
    private static final Duration LOGIN_REFILL = Duration.ofMinutes(1);

    /** 회원가입 : 1분안에 최대 3회 */
    private static final int REGISTER_CAPACITY = 3;
    private static final Duration REGISTER_REFILL = Duration.ofMinutes(1);

    /** 일반 API: 1분 안에 최대 100회 */
    private static final int API_CAPACITY = 100;
    private static final Duration API_REFILL = Duration.ofMinutes(1);

    // 버킷 저장소
    /** key: "login:{ip}", "register:{ip},"api:{ip}" "*/
    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();

    /** 마지막 사용 시각을 함께 보관해 TTl 정리에 사용 */
    private record BucketEntry(Bucket bucket,long lastUsedMs){
        BucketEntry touch(){
            return new BucketEntry(bucket, System.currentTimeMillis());
        }
    }

    // 스케줄러 : 1시간 마다 만료 버킷 제거
    static {
        Thread cleaner = new Thread(() ->{
            while (!Thread.currentThread().isInterrupted()){
                try{
                    Thread.sleep(Duration.ofHours(1).toMillis());
                } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        },"rate-limit-cleaner");
        cleaner.setDaemon(true);
        cleaner.start();
    }

    // 필터 본체
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ip = resolveClientIp(request);
        String path = request.getRequestURI();
        String method = request.getMethod();

        // OPTIONS(프리폴라이트)는 Rate Limit 제외
        if("OPTIONS".equalsIgnoreCase(method)){
            filterChain.doFilter(request,response);
            return;
        }

        Bucket bucket = resolveBucket(ip,path,method);

        if(bucket.tryConsume(1)){
            filterChain.doFilter(request,response);
        } else {
            log.warn("[RateLimit] 요청 차단 - ip{}, URI{} ",ip,path);
            sendTooManyRequestsResponse(response,path);
        }
    }

    // 버킷 결정 로직
    private Bucket resolveBucket(String ip, String path, String method){
        String key;
        Bandwidth bandwidth;

        if(LOGIN_PATH.equals(path) && "POST".equalsIgnoreCase(method)){
            key = "login:"+ip;
            bandwidth = buildBandwidth(LOGIN_CAPACITY,LOGIN_REFILL);
        } else if(REGISTER_PATH.equals(path) && "POST".equalsIgnoreCase(method)){
            key = "register:"+ip;
            bandwidth = buildBandwidth(REGISTER_CAPACITY,REGISTER_REFILL);
        } else{
            key = "api:"+ip;
            bandwidth = buildBandwidth(API_CAPACITY,API_REFILL);
        }

        BucketEntry entry = buckets.compute(key, (k,existing) ->{
            if(existing == null){
                return new BucketEntry(Bucket.builder().addLimit(bandwidth).build(),
                        System.currentTimeMillis());
            }
            return existing.touch();
        });
        return entry.bucket();
    }

    // 유틸
    private Bandwidth buildBandwidth(int capacity, Duration refillPeriod){
        return Bandwidth.classic(capacity,Refill.intervally(capacity,refillPeriod));
    }

    /**
     * 실제 클라이언트 IP 추출
     * 리버스 프록시(Nginx 등) 환경에서 X-Forwarded-For 헤더를 우선 적용
     */
    private String resolveClientIp(HttpServletRequest request){
        String forwarded = request.getHeader("X-Forwarded-For");
        if(forwarded != null && !forwarded.isBlank()){
            // "client, proxy1, proxy2" 형식일 경우 첫 번째(실 클라이언트) IP 만 사용
            return forwarded.split(",")[0].trim();
        }
        String realIP = request.getHeader("X-Real-IP");
        if(realIP != null && !realIP.isBlank()) return realIP.trim();
        return request.getRemoteAddr();
    }

    private void sendTooManyRequestsResponse(HttpServletResponse response, String path) throws IOException{
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After","60");

        String message = LOGIN_PATH.equals(path) ? "로그인 시도 횟수를 초과했습니다. 1분 후 다시 시도해 주세요."
                : "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.";

        response.getWriter().write(
                String.format("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"%s\"}", message)
        );
    }

    /** Actuator, Swagger 등 내부 경로는 Rate Limit 제외 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
