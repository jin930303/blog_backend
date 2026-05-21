package com.example.demo.controller.report;

import com.example.demo.service.admin.AdminService;
import com.example.demo.service.member.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 신고 접수 컨트롤러
 * 로그인한 사용자라면 누구나 신고 가능
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final AdminService adminService;

    // 신고 접수 (인증된 사용자라면 누구나 가능)

    /**
     *  POST /api/v1/reports/{targetId}
     *  특정 회원 신고 접수
     *
     * @param targetId 신고당한 회원 id
     * @param req      신고 사유, 관련 게시글/댓글 id
     * @param userDetails 신고한 회원 (로그인 필수)
     */
    @PostMapping("/{targetId}")
    public ResponseEntity<Map<String,String >> report(
            @PathVariable Long targetId,
            @RequestBody ReportRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        adminService.report(
                userDetails.getMemberId(), //신고자
                targetId,                  //신고 대상
                req.reason(),
                req.boardId(),
                req.commentId()
        );
        return ResponseEntity.ok(Map.of("message","신고가 접수되었습니다."));
    }

    record ReportRequest(String reason, Long boardId, Long commentId){}
}
