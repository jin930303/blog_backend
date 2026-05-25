package com.example.demo.dto.report;

import com.example.demo.entity.member.MemberEntity;
import com.example.demo.entity.report.ReportEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 어드민 신고 목록 응답 DTO
 * 프론트에서 회원별 집계에 대한 필드를 모두 포함
 */
@Getter
@Builder
public class AdminReportResponseDTO {

    private Long reportId; //신고 ID
    private String reason; //신고 사유

    // 신고당한 회원 정보
    private Long reportedMemberId;
    private String reportedNickname;
    private String memberStatus; // ACTIVE | BLOCKED

    //신고한 회원 정보
    private Long reporterId;
    private String reporterNickname;

    //신고 대상 컨텐츠
    private Long boardId;
    private Long commentId;
    private String targetType;  // "board" | "comment" | "none"

    private LocalDateTime createdAt;
    private boolean contentDeleted; //컨텐츠 삭제 여부 (기본 false)

    public static AdminReportResponseDTO of(ReportEntity report, MemberEntity target,
                                            MemberEntity reporter){
        String targetType = "none";
        if(report.getBoardId() !=null) targetType = "board";
        if(report.getCommentId() !=null) targetType = "comment";

        //contentId : 게시글이면 boardId, 댓글이면 commentId
        return AdminReportResponseDTO.builder()
                .reportId(report.getReportId())
                .reason(report.getReason())
                .reportedMemberId(target.getMemberId())
                .reportedNickname(target.getNickname())
                .memberStatus(target.getStatus())
                .reporterId(reporter.getMemberId())
                .reporterNickname(reporter.getNickname())
                .boardId(report.getBoardId())
                .commentId(report.getCommentId())
                .targetType(targetType)
                .createdAt(report.getCreatedAt())
                .contentDeleted(report.isDeleted())
                .build();
    }
}
