package com.example.demo.dto.board;

import com.example.demo.entity.board.BoardEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record BoardResponse(
        Long boardId,
        String title,
        String contentSummary,
        String nickname,
        String filePath,
        String fileOriginalName,
        Long fileSize,
        LocalDateTime inputDate,
        LocalDateTime modifiedDate,
        String content,
        int likes,
        int views,
        String category,

        Boolean isAuthor
) {
    private static final String SERVER_BASE_URL = "http://localhost:8000";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");

    // =========================================================================
    // 💡 1. 오버로딩 메서드 추가 (List.stream().map()에서 사용)
    // =========================================================================
    public static BoardResponse fromEntity(BoardEntity entity, boolean isAuthor) {
        // 목록 조회 시 엔티티의 Content를 그대로 사용하도록 합니다.
        // 상세 조회 시에만 contentOverride를 사용하므로, 이 경우 null을 전달하여 엔티티의 내용을 사용하도록 위임합니다.
        // 또는 contentOverride를 'Optional'로 만들어서 처리할 수 있지만, 여기서는 단순하게 entity.getContent()를 전달합니다.
        return fromEntity(entity, entity.getContentSummary(),isAuthor);
    }

    public static BoardResponse fromEntity(BoardEntity entity,String contentOverrideOrSummary, boolean isAuthor){
        String webFilePath = null;

        if(entity.getFilePath() !=null && !entity.getFilePath().isEmpty()) {
            String fileName = entity.getFilePath().substring(entity.getFilePath().lastIndexOf("\\") + 1);
            webFilePath = SERVER_BASE_URL+"/upload/" + fileName;
        }

        return  new BoardResponse(
                entity.getBoardId(),
                entity.getTitle(),
                contentOverrideOrSummary,
                entity.getNickname(),
                webFilePath,
                entity.getFileOriginalName(),
                entity.getFileSize(),
                entity.getInputDate() !=null ? entity.getInputDate() : LocalDateTime.now(),
                entity.getModifiedDate() !=null ? entity.getInputDate() : LocalDateTime.now(),
                entity.getContent(),
                entity.getLikes(),
                entity.getViews(),
                entity.getCategory(),
                isAuthor

        );
    }

}
