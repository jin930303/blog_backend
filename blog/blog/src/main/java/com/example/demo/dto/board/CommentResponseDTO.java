package com.example.demo.dto.board;

import com.example.demo.entity.board.BoardCommentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {

    private Long commentId;
    private String content;
    private LocalDateTime inputDate;
    private LocalDateTime modifiedDate;
    private Boolean deleted;
    private Integer likes;
    private Long memberId;
    private String nickname;
    private Boolean isAuthor;
    private Boolean isLikedByCurrentUser;

    private List<CommentResponseDTO> children;

    public static CommentResponseDTO fromEntity(BoardCommentEntity entity, Long currentMemberId){
        boolean isAuthor = (currentMemberId != null) && entity.getMember().getMemberId() == currentMemberId;

        List<CommentResponseDTO> childDTOs = entity.getChildren().stream()
                .map(child -> fromEntity(child,currentMemberId))
                .toList();

        return CommentResponseDTO.builder()
                .commentId(entity.getCommentId())
                .content(entity.getContent())
                .nickname(entity.getMember().getNickname())
                .memberId(entity.getMember().getMemberId())
                .inputDate(entity.getInputDate())
                .isAuthor(isAuthor)
                .children(childDTOs)
                .build();
    }
}
