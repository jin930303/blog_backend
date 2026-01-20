package com.example.demo.entity.board;

import com.example.demo.entity.member.MemberEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "board_comment")
@SequenceGenerator(name = "board_comment_seq",sequenceName = "board_comment_seq",allocationSize = 1)
@Builder
public class BoardCommentEntity {
    @Id
    @GeneratedValue(generator = "board_comment_seq",strategy = GenerationType.SEQUENCE)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(nullable = false,length = 500)
    private String content;

    @Column(name = "input_date",nullable = false)
    private LocalDateTime inputDate;
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",nullable = false)
    private MemberEntity member;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted =false;

    @Column(nullable = false)
    @Builder.Default
    private Integer likes = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id",nullable = false)
    private BoardEntity board;


    @PrePersist
    public void prePersist(){
        this.inputDate=LocalDateTime.now();
        if(this.deleted == null){
            this.deleted = false;
        }
        if(this.likes == null){
            this.likes = 0;
        }
    }

    @PreUpdate
    public void preUpdate(){
        this.modifiedDate = LocalDateTime.now();
    }
}
