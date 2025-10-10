package com.example.demo.dto.board;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Setter
@Getter
public class BoardDTO {
    private Long board_id;
    private String title;
    private String content;
    private String author;
    private String filePath;
    private String fileOriginalName;
    private Long fileSize;
    private LocalDateTime inputDate;
    private LocalDateTime modifiedDate;
    private MultipartFile img;
    private int views;
    private int likes;



}
