package com.example.demo.controller.member;

import com.example.demo.dto.member.LoginRequestDTO;
import com.example.demo.dto.member.MemberDTO;
import com.example.demo.service.member.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class MemberRestController {

    private final MemberService memberService;

    private final AuthenticationManager authenticationManager;

    public MemberRestController(MemberService memberService, AuthenticationManager authenticationManager) {
        this.memberService = memberService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDTO loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String nickname = memberService.getNicknameByUsername(loginRequest.getUsername());

        Map<String, String> responseBody = new HashMap<>();

        responseBody.put("username", loginRequest.getUsername());
        responseBody.put("nickname", nickname);
        responseBody.put("message", "로그인 성공");

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/member")
    public ResponseEntity<Map<String, String>> signup(@RequestBody MemberDTO dto) {
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
}
