package com.example.demo.dto.board;

import com.example.demo.entity.board.BoardEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record BoardResponse(
        Long boardId,
        String title,
        String content,
        String nickname,
        String filePath,
        String fileOriginalName,
        Long fileSize,
        String inputDate,
        String modifiedDate,
        int likes,
        int views,
        String category
) {
    public static BoardResponse fromEntity(BoardEntity entity){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");

        return  new BoardResponse(
                entity.getBoardId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getNickname(),
                entity.getFilepath(),
                entity.getFileOriginalName(),
                entity.getFileSize(),
                entity.getInputDate() !=null ? entity.getInputDate().format(formatter) : String.valueOf(LocalDateTime.now()),
                entity.getModifiedDate() !=null ? entity.getModifiedDate().format(formatter) : String.valueOf(LocalDateTime.now()),
                entity.getLikes(),
                entity.getViews(),
                entity.getCategory()

        );
    }

}
