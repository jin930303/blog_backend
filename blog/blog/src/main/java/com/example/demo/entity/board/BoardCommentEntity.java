package com.example.demo.entity.board;

import com.example.demo.entity.member.MemberEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "board_comment")
@SequenceGenerator(name = "board_comment_seq",sequenceName = "board_comment_seq",allocationSize = 1)
public class BoardCommentEntity {
    @Id
    @GeneratedValue(generator = "board_comment_seq",strategy = GenerationType.SEQUENCE)
    @Column(name = "comment_id")
    private Long commentId;

    private String content;

    private LocalDateTime inputDate;
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",nullable = false)
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id",nullable = false)
    private BoardEntity board;

    @PrePersist
    public void prePersist(){
        this.inputDate=LocalDateTime.now();
    }
}
