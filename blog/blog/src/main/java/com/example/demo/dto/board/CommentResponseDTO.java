package com.example.demo.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
