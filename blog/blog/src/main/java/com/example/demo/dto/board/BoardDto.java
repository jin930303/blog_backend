package com.example.demo.dto.board;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class BoardDto {
    private Long board_id;
    private String title;
    private String content;
    private String author;
    private String filePath;
    private String fileOriginalName;
    private Long fileSize;
    private LocalDateTime inputDate;



}
