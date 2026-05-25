package com.example.demo.entity.report;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor
@SequenceGenerator(name = "report_seq",sequenceName = "report_seq",allocationSize = 1)
public class ReportEntity {

    @Id
    @Column(name = "report_id")
    @GeneratedValue(generator = "report_seq",strategy = GenerationType.SEQUENCE)
    private Long reportId;

    @Column(name = "reporter_id",nullable = false)
    private Long reporterId;

    @Column(name = "target_id",nullable = false)
    private Long targetId;

    @Column(name = "reason",nullable = false,length = 500)
    private String reason;

    @Column(name = "board_id")
    private Long boardId;

    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted",nullable = false)
    private boolean deleted = false;


    @Builder
    public ReportEntity(Long reporterId, Long targetId, String reason, Long boardId, Long commentId){
        this.reporterId = reporterId;
        this.targetId = targetId;
        this.reason = reason;
        this.boardId = boardId;
        this.commentId = commentId;
    }

    @PrePersist
    public void prePersist(){
        if(this.createdAt == null){
            this.createdAt = LocalDateTime.now();
        }
        if(!this.deleted){
            this.deleted = false;
        }
    }
    public void markContentDeleted(){
        this.deleted=true;
    }
}
