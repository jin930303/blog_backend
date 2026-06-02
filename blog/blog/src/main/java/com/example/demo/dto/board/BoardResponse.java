package com.example.demo.dto.board;

import com.example.demo.entity.board.BoardEntity;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record BoardResponse(
        Long boardId,
        String title,
        String contentSummary,
        String nickname,
        String filePath, // 💡 DB의 원본 파일명
        String fileOriginalName,
        Long fileSize,
        LocalDateTime inputDate,
        LocalDateTime modifiedDate,
        String content, // 💡 리스트 조회시는 null
        int likes,
        int views,
        String category,
        List<String> tags,
        Boolean isAuthor
) {
    private static final String SERVER_BASE_URL = "http://localhost:8000";

    // ✅ [핵심] 컴팩트 생성자
    // JPQL 조회(new BoardResponse...)와 fromEntity 모두 이 로직을 거칩니다.
    public BoardResponse {
        // 이미지 경로 변환 로직
        if (filePath != null && !filePath.isEmpty() && !filePath.startsWith("http")) {
            String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
            filePath = SERVER_BASE_URL + "/upload/" + fileName;
        }

        // 태그 null 방지
        if (tags == null) {
            tags = Collections.emptyList();
        }
    }

    // =========================================================================
    // 💡 기존 서비스 코드 호환용 오버로딩 메서드들 (이게 없어서 에러가 났던 것!)
    // =========================================================================

    private static String resolveNickname(BoardEntity entity){
        if(entity.getMember() != null){
            return entity.getMember().getNickname();
        }
        return null;
    }


    // 1. (Entity, Tags, Summary, isAuthor) - 상세 조회용
    public static BoardResponse fromEntity(BoardEntity entity, List<String> tags, String summary, boolean isAuthor) {
        return new BoardResponse(
                entity.getBoardId(),
                entity.getTitle(),
                summary, // 전달받은 요약 사용
                resolveNickname(entity),
                entity.getFilePath(),
                entity.getFileOriginalName(),
                entity.getFileSize(),
                entity.getInputDate() != null ? entity.getInputDate() : LocalDateTime.now(),
                entity.getModifiedDate() != null ? entity.getModifiedDate() : LocalDateTime.now(),
                entity.getContent(),
                entity.getLikes(),
                entity.getViews(),
                entity.getCategory(),
                tags,
                isAuthor
        );
    }
    // 목록/검색용 (content 제외)
    public static BoardResponse fromEntitySummary(BoardEntity entity, List<String> tags, String summary, boolean isAuthor){
        return new BoardResponse(
                entity.getBoardId(),
                entity.getTitle(),
                summary,
                resolveNickname(entity),
                entity.getFilePath(),
                entity.getFileOriginalName(),
                entity.getFileSize(),
                entity.getInputDate() != null ? entity.getInputDate() : LocalDateTime.now(),
                entity.getModifiedDate() !=null ? entity.getModifiedDate() : LocalDateTime.now(),
                null,
                entity.getLikes(),
                entity.getViews(),
                entity.getCategory(),
                tags,
                isAuthor

        );
    }


    // 2. (Entity, Tags, isAuthor) - 3개 인자 버전
    public static BoardResponse fromEntity(BoardEntity entity, List<String> tags, boolean isAuthor) {
        return fromEntity(entity, tags, entity.getContentSummary(), isAuthor);
    }

    // 3. (Entity, isAuthor) - 2개 인자 버전 (태그 없을 때)
    public static BoardResponse fromEntity(BoardEntity entity, boolean isAuthor) {
        return fromEntity(entity, Collections.emptyList(), entity.getContentSummary(), isAuthor);
    }
}