package com.example.demo.dto.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardSearchDTO {
    private Long boardId;
    private String title;
    private String contentSummary;
    private String nickname;
    private String filePath;
    private LocalDateTime inputDate;
    private LocalDateTime modifiedDate;
    private int likes;
    private int views;
    private String category;
    private Long memberId;
}
