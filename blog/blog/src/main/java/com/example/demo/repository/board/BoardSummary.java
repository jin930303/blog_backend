package com.example.demo.repository.board;


import java.time.LocalDateTime;

public interface BoardSummary {

    Long getBoardId();
    String getTitle();
    String getContentSummary();
    String getNickname();
    String getFilePath();
    String getFileOriginalName();
    Long getFileSize();
    LocalDateTime getInputDate();
    LocalDateTime getModifiedDate();
    Integer getViews();
    Integer getLikes();
    String getCategory();

}
