package com.example.demo.dto.board;

import com.example.demo.entity.board.BoardEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class BoardDetailResponse {
    private Long boardId;
    private String title;
    private String content;
    private int views;
    private LocalDateTime inputDate;
    private LocalDateTime modifiedDate;
    private int likes;

    private Member member;

    private Boolean isAuthor;

    private Boolean isLikedByCurrentUser;
    private List<String> tags;

    @Getter
    @Builder
    public static class Member{
        private Long memberId;
        private String nickname;
    }

    public static BoardDetailResponse of(BoardEntity board, Boolean isAuthor, Boolean isLiked, List<String> tags){
        return BoardDetailResponse.builder()
                .boardId(board.getBoardId())
                .title(board.getTitle())
                .content(board.getContent())
                .views(board.getViews())
                .likes(board.getLikes())
                .inputDate(board.getInputDate())
                .modifiedDate(board.getModifiedDate())
                .member(Member.builder()
                        .memberId(board.getMember().getMemberId())
                        .nickname(board.getMember().getNickname())
                        .build())
                .isAuthor(isAuthor)
                .isLikedByCurrentUser(isLiked)
                .tags(tags)
                .build();

    }
}
