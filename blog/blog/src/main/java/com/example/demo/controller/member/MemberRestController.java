package com.example.demo.controller.member;

import com.example.demo.dto.member.LoginRequestDTO;
import com.example.demo.dto.member.MemberDTO;
import com.example.demo.service.member.CustomUserDetails;
import com.example.demo.service.member.JwtTokenProvider;
import com.example.demo.service.member.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class MemberRestController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public MemberRestController(MemberService memberService, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {

        // 1. 인증 객체 생성 및 검증
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 2. 인증 객체에서 CustomUserDetails 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 3. 토큰 생성에 필요한 정보 추출
        String username = userDetails.getUsername();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        String nickname = memberService.getNicknameByUsername(username);

        // 4. JWT 토큰 생성
        String jwtToken = jwtTokenProvider.createToken(username, role, nickname); // 토큰 생성

        // 쿠키 보안 강화하기 HttpOnly
        jwtTokenProvider.addTokenToCookie(response, jwtToken);

        // 5. 응답 구성
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("username", username);
        responseBody.put("nickname", nickname);
        responseBody.put("message", "로그인 성공");
        // responseBody.put("token", jwtToken); // 토큰을 응답 바디에 포함

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        // 1. HttpOnly 쿠키 삭제를 위해 Max-Age를 0으로 설정
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 쿠키의 유효기간을 0으로 설정하여 즉시 삭제

        // 2. 응답에 쿠키를 추가하여 브라우저에 삭제 명령 전달
        response.addCookie(cookie);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "로그아웃 성공. 쿠키가 삭제되었습니다.");
        return ResponseEntity.ok(responseBody);
    }

//    @PostMapping("/login")
//    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDTO loginRequest) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        loginRequest.getUsername(),
//                        loginRequest.getPassword()
//                )
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        String nickname = memberService.getNicknameByUsername(loginRequest.getUsername());
//
//        Map<String, String> responseBody = new HashMap<>();
//
//        responseBody.put("username", loginRequest.getUsername());
//        responseBody.put("nickname", nickname);
//        responseBody.put("message", "로그인 성공");
//
//        return ResponseEntity.ok(responseBody);
//    }

    @PostMapping("/member")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody MemberDTO dto) {

        // 아이디 중복 체크 추가
        if(memberService.checkUsername(dto.getUsername())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","이미 사용 중인 아이디입니다."));
        }

        // 닉네임 중복 체크 추가
        if(memberService.checkNickname(dto.getNickname())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","이미 사용 중인 닉네임입니다."));
        }


        memberService.signup_save(dto);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "회원가입 성공");
        responseBody.put("username", dto.getUsername()); // ID 대신 username 반환

        return ResponseEntity.status(201)
                .body(responseBody);
    }

    @PostMapping("/check/id")
    public ResponseEntity<Map<String, String>> signup01(@RequestBody Map<String, String> request) {
        String username =request.get("id");

        if (memberService.checkUsername(username)){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "이미 사용 중인 아이디입니다."));
        } else {
            return ResponseEntity.ok(Map.of("message", "사용 가능한 아이디입니다."));
        }
    }

    @PostMapping("/check/nickname")
    public ResponseEntity<Map<String, String>> signup02(@RequestBody Map<String, String> request) {
        String nickname = request.get("nick");

        if(memberService.checkNickname(nickname)){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "이미 사용중인 닉네임입니다."));
        } else  {
            return ResponseEntity.ok(Map.of("message", "사용 가능한 닉네임입니다."));
        }
    }

    @GetMapping("/mypage")
    public ResponseEntity<Map<String, Object>> mypage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Map<String, Object> responseBody = new HashMap<>();

        if (customUserDetails != null) {
            try {
                // 1. JWT에서 가져온 정보 memberId, username
                Long memberId = customUserDetails.getMemberId();
                String username = customUserDetails.getUsername();

                // 2. DB에서 추가 정보 조회 하기
                MemberDTO memberDTO = memberService.getMemberInfoById(memberId);

                responseBody.put("memberId", memberId);
                responseBody.put("username", username);
                responseBody.put("nickname", memberDTO.getNickname());
                responseBody.put("email", memberDTO.getEmail());
                responseBody.put("message", "마이페이지 정보 조회 성공.");
                return ResponseEntity.ok(responseBody);

            } catch (Exception e) {
                // DB 조회 또는 데이터 처리 오류
                responseBody.put("message", "사용자 정보 조회 중 오류 발생");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
            }
        } else {
            responseBody.put("message", "로그인 세션이 유효하지 않음");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
        }
    }
}
