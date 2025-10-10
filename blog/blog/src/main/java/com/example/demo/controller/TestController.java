package com.example.demo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@Tag(name = "여기는 ~~")
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class TestController {

    record BoardDto(Long id, String title, String author){}

    @GetMapping("/test")
    public String helloWorld() {
        return "Backend API is running on port 8000!";
    }

    @GetMapping
    public List<BoardDto> getBoardList() {
        // 실제로는 서비스 계층에서 DB 조회를 하겠지만, 초기 테스트를 위해 Mock 데이터를 반환합니다.
        return List.of(
                new BoardDto(1L, "React 대신 Vanilla JS로 시작합니다", "김개발"),
                new BoardDto(2L, "Spring Boot API 연동 테스트 성공!", "박프론트"),
                new BoardDto(3L, "fetch API를 사용한 비동기 통신", "최백엔드")
        );
    }
}
