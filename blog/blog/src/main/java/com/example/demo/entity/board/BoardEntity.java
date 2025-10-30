package com.example.demo.entity.board;

import com.example.demo.entity.member.MemberEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Table(name = "board")
@SequenceGenerator(name = "board_seq",sequenceName = "board_seq",initialValue = 1,allocationSize = 1)
public class BoardEntity {
    @Id
    @Column(name = "board_id")
    @GeneratedValue(generator = "board_seq",strategy = GenerationType.SEQUENCE)
    private Long boardId;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;

    private String nickname;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_original_name")
    private String fileOriginalName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "input_date",updatable = false)
    private LocalDateTime inputDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    private int likes;
    private int views;
    private String category;

    @Column(name = "content_summary")
    private String contentSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",nullable = true)
    private MemberEntity member;

    private Long getAuthorMemberId(){
        return this.member != null ? this.member.getMemberId() : null;
    }

    public void update(
            String title,
            String nickname,
            String content,
            String category,
            String fileOriginalName,
            Long fileSize,
            String filePath,
            String contentSummary

    ){
        this.title = title;
        this.nickname = nickname;
        this.content=content;
        this.category=category;
        this.modifiedDate=LocalDateTime.now();

        this.fileOriginalName = fileOriginalName;
        this.fileSize=fileSize;
        this.filePath=filePath;
        this.contentSummary = contentSummary;

    }
}
