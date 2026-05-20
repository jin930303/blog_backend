package com.example.demo.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

/**
 * RateLimitingFilter 단위 테스트
 *
 * MockHttpServletRequest/Response 를 사용해 서버 없이 필터 로직만 검증
 * 테스트마다 새 필터 인스턴스를 생성해 버킷 상태가 격리되도록 설계
 */
@DisplayName("RateLimitingFilter 테스트")
public class RateLimitingFilterTest {

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp(){
        //테스트 마다 버킷 초기화(인스턴스 새로 생성)
        filter = new RateLimitingFilter();
    }

    // 공통 헬퍼
    private MockHttpServletRequest makeRequest(String method, String uri, String ip){
        MockHttpServletRequest request = new MockHttpServletRequest(method,uri);
        request.setRemoteAddr(ip);
        return request;
    }

    /**
     * 필터를 n번 호출하고 마지막 응답을 반환
     * 중간 응답의 status 는 무시 (마지막 것만 검증하는 경우에 사용)
     */
    private MockHttpServletResponse callFilter(MockHttpServletRequest request, int times) throws Exception {
        MockHttpServletResponse response = null;
        for(int i = 0 ; i<times ; i++){
            response = new MockHttpServletResponse();
            filter.doFilter(request,response,new MockFilterChain());
        }
        return response;
    }

    // 1. 로그인 Rate Limit  테스트 (5회/분)

    @Nested
    @DisplayName("로그인 엔드포인트 (/api/v1/login)")
    class LoginRateLimit{
        private static final String IP = "1.2.3.4";
        private static final String PATH = "/api/v1/login";

        @Test
        @DisplayName("5회 이하 요청은 200 통과")
        void allow_within_limit() throws Exception{
            MockHttpServletRequest request = makeRequest("POST",PATH,IP);

            for(int i=1; i<=5; i++){
                MockHttpServletResponse response = new MockHttpServletResponse();
                filter.doFilter(request,response, new MockFilterChain());
                assertThat(response.getStatus()).as("%d번째 요청은 통과해야 함",i)
                        .isNotEqualTo(429);
            }
        }

        @Test
        @DisplayName("6번째 요청은 429 차단")
        void block_6th_request() throws Exception{
            MockHttpServletRequest request = makeRequest("POST",PATH,IP);

            //5회 소진
            callFilter(request,5);

            // 6번째
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request,response,new MockFilterChain());

            assertThat(response.getStatus()).isEqualTo(429);
        }

        @Test
        @DisplayName("429 응답 본문에 한국어 메시지 포함")
        void block_response_body_contains_message() throws Exception{
            MockHttpServletRequest request = makeRequest("POST",PATH,IP);
            callFilter(request,5);

            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request,response,new MockFilterChain());

            String body = response.getContentAsString();
            assertThat(body).contains("로그인 시도 횟수를 초과했습니다.");
        }

        @Test
        @DisplayName("429 응답에 Retry-After: 60 헤더 포함")
        void block_response_has_retry_after_header() throws Exception{
            MockHttpServletRequest request = makeRequest("POST",PATH,IP);
            callFilter(request,5);

            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request,response,new MockFilterChain());

            assertThat(response.getHeader("Retry-After")).isEqualTo("60");
        }

        @Test
        @DisplayName("IP가 다르면 버킷이 분리되어 각각 5회 허용")
        void different_ips_have_separate_buckets() throws Exception{
            MockHttpServletRequest reqA = makeRequest("POST",PATH,"10.0.0.1");
            MockHttpServletRequest reqB = makeRequest("POST",PATH,"10.0.0.2");

            // IP-A 5회 소진
            callFilter(reqA,5);

            // IP-B는 아직 0회 -> 통과해야 함
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(reqB,response,new MockFilterChain());

            assertThat(response.getStatus()).isNotEqualTo(429);
        }
    }

    // 2. 회원가입 Rate limit 테스트 (3회/분)

    @Nested
    @DisplayName("회원가입 엔드포인트 (api/v1/member POST)")
    class RegisterRateLimit{
        private static final String IP = "5.6.7.8";
        private static final String PATH = "/api/v1/member";

        @Test
        @DisplayName("3회 이하 요청은 통과")
        void allow_within_limit() throws Exception{
            MockHttpServletRequest request = makeRequest("POST",PATH,IP);

            for(int i=0; i<3;i++){
                MockHttpServletResponse response = new MockHttpServletResponse();
                filter.doFilter(request,response,new MockFilterChain());
                assertThat(response.getStatus()).as("%d번째 요청은 통과해야 함",i)
                        .isNotEqualTo(429);
            }
        }

        @Test
        @DisplayName("4번째 요청은 429 차단")
        void block_4th_request() throws Exception{
            MockHttpServletRequest request = makeRequest("POST",PATH,IP);
            callFilter(request,3);

            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request,response,new MockFilterChain());

            assertThat(response.getStatus()).isEqualTo(429);
        }

        @Test
        @DisplayName("GET /api/v1/member 는 회원가입 버킷이 아닌 일반 버킷 적용")
        void get_method_uses_api_bucket_not_register() throws Exception {
            // POST 3회 소진 (회원가입 버킷)
            MockHttpServletRequest postReq = makeRequest("POST", PATH, IP);
            callFilter(postReq,3);

            //GET은 별도 버킷 -> 차단되면 안됨
            MockHttpServletRequest getReq = makeRequest("GET",PATH,IP);
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(getReq,response,new MockFilterChain());

            assertThat(response.getStatus()).isNotEqualTo(429);
        }
    }

    // 3. 일반 API Rate Limit 테스트 (100회/분)

    @Nested
    @DisplayName("일반 API 엔드포인트")
    class ApiRateLimit{

        private static final String IP = "9.10.11.12";

        @Test
        @DisplayName("100회 이하 요청은 통과")
        void allow_100_requests() throws Exception{
            MockHttpServletRequest request = makeRequest("GET","/api/v1/boards/cursor",IP);
            callFilter(request,100);

            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request,response,new MockFilterChain());

            assertThat(response.getStatus()).isEqualTo(429);
        }

        @Test
        @DisplayName("429 응답 본문에 일반 메시지 포함")
        void block_response_body_contains_general_message() throws Exception{
            MockHttpServletRequest request = makeRequest("GET","/api/v1/boards/cursor",IP);
            callFilter(request,100);

            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request,response,new MockFilterChain());

            assertThat(response.getContentAsString()).contains("요청이 너무 많습니다.");
        }
    }

    // 4. 필터 제외 경로 테스트

    @Nested
    @DisplayName("shouldNotFilter -Rate Limit 제외 경로")
    class ShouldNotFilter{

        @Test
        @DisplayName("/actuator/** 는 필터 제외")
        void actuator_excluded() throws Exception{
            MockHttpServletRequest request = makeRequest("GET","/actuator/health","1.1.1.1");
            assertThat(filter.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("/swagger-ui/**는 필터 제외")
        void swagger_excluded() throws Exception{
            MockHttpServletRequest request = makeRequest("GET","/swagger-ui/index.html","1.1.1.1");
            assertThat(filter.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("/v3/api-docs/** 는 필터 제외")
        void api_docs_excluded() throws Exception{
            MockHttpServletRequest request = makeRequest("GET","/v3/api-docs/swagger-config","1.1.1.1");
            assertThat(filter.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("/api/v1/login 은 필터 제외 아님")
        void login_not_excluded() throws Exception{
            MockHttpServletRequest request = makeRequest("GET","/api/v1/login","1.1.1.1");

            for(int i =0; i<100; i++){
                MockHttpServletResponse response = new MockHttpServletResponse();
                filter.doFilter(request,response,new MockFilterChain());
                assertThat(response.getStatus()).isNotEqualTo(429);
            }
        }
    }

    @Nested
    @DisplayName("X-Forwarded-For 헤더 IP 추출")
    class ForwardIp{

        @Test
        @DisplayName("X-Forwarded-For 첫 번째 IP를 클라이언트 IP로 사용")
        void uses_first_forwarded_ip() throws Exception{
            // IP-A (Forwarded) 로 5회 소진
            for (int i = 0 ; i<5; i++){
                MockHttpServletRequest req =makeRequest("POST","/api/v1/login","proxy-ip");
                req.addHeader("X-Forwarded-For","111.111.111.111, proxy1, proxy2");
                MockHttpServletResponse res = new MockHttpServletResponse();
                filter.doFilter(req,res,new MockFilterChain());
            }

            // 6번째 - 같은 forwarded ip -> 차단
            MockHttpServletRequest req = makeRequest("POST","/api/v1/login","proxy-ip");
            req.addHeader("X-Forwarded-For","111.111.111.111, proxy1, proxy2");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req,res,new MockFilterChain());

            assertThat(res.getStatus()).isEqualTo(429);

        }

        @Test
        @DisplayName("X-Forwarded-For IP가 달라지면 별도 버킷 적용")
        void different_forwarded_ips_separate_buckets() throws Exception{
            //IP-A 5회 소진
            for(int i = 0 ; i<5; i++){
                MockHttpServletRequest request = makeRequest("POST","/api/v1/login","proxy-ip");
                request.addHeader("X-Forwarded-For","222.222.222.222");
                filter.doFilter(request,new MockHttpServletResponse(), new MockFilterChain());
            }
            // IP-B는 별도 버킷 -> 통과
            MockHttpServletRequest request = makeRequest("POST","/api/v1/login","proxy-ip");
            request.addHeader("X-Forwarded_For","333.333.333.333");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request,response,new MockFilterChain());

            assertThat(response.getStatus()).isNotEqualTo(429);
        }
    }
}
