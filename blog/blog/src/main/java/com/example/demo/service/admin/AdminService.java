package com.example.demo.service.admin;

import com.example.demo.dto.report.AdminReportResponseDTO;
import com.example.demo.entity.report.ReportEntity;

import java.util.List;

public interface AdminService {
    void deleteBoard(Long boardId);

    void deleteComment(Long commentId);

    void blockMember(Long memberId);

    List<ReportEntity> getReports(Long memberId);

    void report(Long memberId, Long targetId, String reason, Long boardId, Long commentId);

    void unblockMember(Long memberId);

    List<AdminReportResponseDTO> getAllReports();
}
