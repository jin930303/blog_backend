package com.example.demo.controller.admin;

import com.example.demo.dto.report.AdminReportResponseDTO;
import com.example.demo.entity.report.ReportEntity;
import com.example.demo.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 어드민 전용 컨트롤러
 * SecurityConfig 에서 /api/v1/admin/** -> hasRole("ADMIN") 으로 보호됨
 * @PreAuthrize 이중 방어선 역할
 */
@RestController
@RequestMapping("/api/v1/boards/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // 게시글 강제 삭제

    /**
     * DELETE /api/v1/boards/admin/{boardId}
     */

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Map<String,String>> deleteBoard(@PathVariable Long boardId){
        adminService.deleteBoard(boardId);
        return ResponseEntity.ok(Map.of("message","게시글이 삭제되었습니다."));

    }

    // 댓글 강제 삭제

    /**
     *  DELETE /api/v1/boards/admin/comments/{commentId}
     */

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Map<String,String>> deleteComment(@PathVariable Long commentId){
        adminService.deleteComment(commentId);
        return ResponseEntity.ok(Map.of("message","댓글이 삭제되었습니다."));
    }

    // 수동 차단
    /**
     * POST /api/v1/boards/admin/members/{memberId}/block
     */

    @PostMapping("/members/{memberId}/block")
    public ResponseEntity<Map<String,String >> blockMember(@PathVariable Long memberId){
        adminService.blockMember(memberId);
        return ResponseEntity.ok(Map.of("message","회원이 차단되었습니다."));
    }

    /**
     *  DELETE /api/v1/boards/admin/members/{memberId}/block
     */
    @DeleteMapping("/members/{memberId}/block")
    public ResponseEntity<Map<String,String >> unblockMember(@PathVariable Long memberId){
        adminService.unblockMember(memberId);
        return ResponseEntity.ok(Map.of("message","차단이 해제되었습니다."));
    }

    // 신고 내역 조회
    /**
     * GET /api/v1/boards/admin/members/{memberId}/reports
     */
    @GetMapping("/members/{memberId}/reports")
    public ResponseEntity<List<ReportEntity>> getReports(@PathVariable Long memberId){
        return ResponseEntity.ok(adminService.getReports(memberId));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<AdminReportResponseDTO>> getAllReports(){
        return ResponseEntity.ok(adminService.getAllReports());
    }
}

