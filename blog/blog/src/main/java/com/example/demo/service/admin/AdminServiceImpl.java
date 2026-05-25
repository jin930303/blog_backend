package com.example.demo.service.admin;

import com.example.demo.dto.report.AdminReportResponseDTO;
import com.example.demo.entity.board.BoardCommentEntity;
import com.example.demo.entity.board.BoardEntity;
import com.example.demo.entity.member.MemberEntity;
import com.example.demo.entity.report.ReportEntity;
import com.example.demo.repository.board.BoardCommentRepository;
import com.example.demo.repository.board.BoardRepository;
import com.example.demo.repository.member.MemberRepository;
import com.example.demo.repository.report.ReportRepository;
import com.example.demo.service.board.BoardSearchService;
import com.example.demo.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 어드민 서비스
 * - 게시글 강제 삭제 : 기존 deleteBoard 와 동일하게 파일 -> ES 색인까지 제거
 * - 댓글 강제 삭제 : 기존 deleteComment 와 동일하게 소프트 딜리트
 * - 신고 접수 및 누적 3회 자동 차단
 * - 수동 차단 / 차단 해제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final BoardRepository boardRepository;
    private final BoardCommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;
    private final FileService fileService;
    private final BoardSearchService boardSearchService;

    private static final long BLOCK_THRESHOLD = 3;

    // 게시글 강제 삭제
    @Transactional
    @Override
    public void deleteBoard(Long boardId) {
        BoardEntity board = boardRepository.findById(boardId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 게시글입니다. id="+boardId));

        // 1. 본문 이미지 파일 삭제 (기존 로직과 동일)
        if(board.getContent() !=null && !board.getContent().isEmpty()){
            Set<String> imgUrls =extractImageUrls(board.getContent());
            for(String imgUrl : imgUrls){
                String physicalPath = getPhysicalFilePath(imgUrl);
                if(physicalPath != null){
                    try {
                        fileService.deleteFile(physicalPath);
                        log.info("[ADMIN] 이미지 파일 삭제 성공:{}",physicalPath);
                    } catch (Exception e){
                        log.error("[ADMIN] 이미지 파일 삭제 실패 : {}.{}",physicalPath,e.getMessage());
                    }
                }
            }
        }

        // 2. 첨부 파일 삭제 (기존 로직과 동일)
        if(board.getFilePath() !=null && !board.getFilePath().isEmpty()){
            String pthsicalPath = getPhysicalFilePath(board.getFilePath());
            if(pthsicalPath != null){
                try{
                    fileService.deleteFile(pthsicalPath);
                    log.info("[ADMIN] 첨부 파일 삭제 성공 :{}",pthsicalPath);
                } catch (Exception e){
                    log.error("[ADMIN] 첨부 파일 삭제 실패 : {}, {}",pthsicalPath,e.getMessage());
                }
            }
        }

        // 3. 게시글 하드 딜리트
        boardRepository.deleteById(boardId);
        log.warn("[ADMIN] 게시글 강제 삭제 완료 - boardId={}",boardId);
        reportRepository.findAllByBoardId(boardId).forEach(ReportEntity :: markContentDeleted);

        // 4. ES 색인 삭제
        try{
            boardSearchService.delete(boardId);
        } catch (Exception e){
            log.error("[ADMIN][ES] 게시글 색인 삭제 실패 boardId={}, {}",boardId,e.getMessage());
        }
    }

    // 댓글 강제 삭제
    @Transactional
    @Override
    public void deleteComment(Long commentId) {
        BoardCommentEntity comment = commentRepository.findById(commentId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 댓글입니다. id="+commentId));

        // 기존 방식과 동일하게 소프트 딜리트
        comment.setDeleted(true);
        commentRepository.save(comment);

        reportRepository.findAllByCommentId(commentId).forEach(ReportEntity::markContentDeleted);
        log.warn("[ADMIN] 댓글 강제 삭제 완료 commentId ={}",commentId);

    }

    // 수동 차단
    @Transactional
    @Override
    public void blockMember(Long memberId) {
        MemberEntity member = memberRepository.findById(memberId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 회원입니다. id="+memberId));
        member.block();
        log.warn("[ADMIN] 회원 차단 - memberId={}, userName={}",memberId,member.getUsername());

    }

    // 차단 해제
    @Transactional
    @Override
    public void unblockMember(Long memberId) {
        MemberEntity member = memberRepository.findById(memberId).orElseThrow(()->new IllegalArgumentException("존재하지 않느 회원입니다. id="+memberId));
        member.unblock();
        log.info("[ADMIN] 회원 차단 해제 memberId ={}, username={}",memberId,member.getUsername());

    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminReportResponseDTO> getAllReports() {
        return reportRepository.findAll().stream()
                .map(report -> {
                    MemberEntity target = memberRepository.findById(report.getTargetId())
                            .orElseThrow(()-> new IllegalArgumentException("회원 없음"));
                    MemberEntity reporter = memberRepository.findById(report.getReporterId())
                            .orElseThrow(()-> new IllegalArgumentException("신고자 없음"));
                    return AdminReportResponseDTO.of(report,target,reporter);
                })
                .toList();
    }

    // 신고 내역 조회
    @Transactional
    @Override
    public List<ReportEntity> getReports(Long targetId) {
        return reportRepository.findAllByTargetId(targetId);
    }

    // 신고 접수
    @Transactional
    @Override
    public void report(Long reporterId, Long targetId, String reason, Long boardId, Long commentId) {
        if(reporterId.equals(targetId)){
            throw new IllegalArgumentException("자기 자신을 신고할 수 없습니다.");
        }

        reportRepository.save(ReportEntity.builder()
                .reporterId(reporterId)
                .targetId(targetId)
                .reason(reason)
                .boardId(boardId)
                .commentId(commentId)
                .build());

        long count = reportRepository.countByTargetId(targetId);
        log.info("[ADMIN] 신고 접수 - targetId= {}, 누적횟수 = {}",targetId, count);

        if(count >=BLOCK_THRESHOLD){
            blockMember(targetId);
        }

    }

    // 유틸: 이미지 URL 추출/ 물리 경로 반환 (기존 BoardService 와 동일)

    private Set<String> extractImageUrls(String content){
        Set<String> urls = new HashSet<>();
        Pattern pattern = Pattern.compile("!\\[.*?]\\((.*?)\\)|<img[^>]+src=[\"'](.*?)[\"']");
        Matcher matcher = pattern.matcher(content);
        while(matcher.find()){
            String url = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if(url != null && !url.isBlank()) urls.add(url);
        }
        return urls;
    }

    private String getPhysicalFilePath(String fileUrl){
        if(fileUrl == null || fileUrl.isBlank()) return null;
        String uploadRoot ="C:/upload";
        if(fileUrl.startsWith("/images/")){
            return uploadRoot + fileUrl.substring("/images".length());
        }
        return null;
    }


}
