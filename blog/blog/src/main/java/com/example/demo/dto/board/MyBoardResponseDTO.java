package com.example.demo.dto.board;

import com.example.demo.entity.board.BoardEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyBoardResponseDTO {

    private Long boardId;
    private String title;
    private LocalDateTime inputDate;
    private int views;
    private int likes;
    private String category;

    public static MyBoardResponseDTO from (BoardEntity entity){
        return MyBoardResponseDTO.builder()
                .boardId(entity.getBoardId())
                .title(entity.getTitle())
                .inputDate(entity.getInputDate())
                .views(entity.getViews())
                .likes(entity.getLikes())
                .category(entity.getCategory())
                .build();
    }
}
