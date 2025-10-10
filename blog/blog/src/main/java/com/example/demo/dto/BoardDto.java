package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BoardDto {
    private Long board_id;
    private String title;
    private String author;

    public BoardDto(long l, String 첫_글, String 작성자) {
    }
}
