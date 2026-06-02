package com.example.demo.document;

import com.example.demo.entity.board.BoardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Document(indexName = "boards")
@Setting(settingPath = "elasticsearch/board-settings.json")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDocument {

    @Id
    @Field(type = FieldType.Long)
    private Long boardId;

    @Field(type = FieldType.Text, analyzer = "nori_edge_ngram",searchAnalyzer = "nori_edge_ngram")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori_edge_ngram",searchAnalyzer = "nori_edge_ngram")
    private String contentSummary;

    @Field(type = FieldType.Keyword)
    private String nickname;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Integer)
    private int likes;

    @Field(type = FieldType.Integer)
    private int views;

    @Field(type = FieldType.Date, format = {DateFormat.date_hour_minute_second, DateFormat.date})
    private LocalDateTime inputDate;

    @Field(type = FieldType.Date, format = {DateFormat.date_hour_minute_second, DateFormat.date})
    private LocalDateTime modifiedDate;

    // 태그 검색용
    @Field(type = FieldType.Keyword)
    private List<String> hashtags;

    // BoardEntity → BoardDocument 변환
    public static BoardDocument from(BoardEntity board) {
        List<String> tags = board.getBoardHashtags().stream()
                .map(bh -> bh.getHashtag().getName())
                .collect(Collectors.toList());

        String nickname = (board.getMember() !=null) ? board.getMember().getNickname() : null;

        return BoardDocument.builder()
                .boardId(board.getBoardId())
                .title(board.getTitle())
                .contentSummary(board.getContentSummary())
                .nickname(nickname)
                .category(board.getCategory())
                .likes(board.getLikes())
                .views(board.getViews())
                .inputDate(board.getInputDate())
                .modifiedDate(board.getModifiedDate())
                .hashtags(tags)
                .build();
    }
}
