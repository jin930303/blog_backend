package com.example.demo.controller.board;

import com.example.demo.dto.board.*;
import com.example.demo.entity.board.BoardEntity;
import com.example.demo.entity.board.BoardHashtagEntity;
import com.example.demo.repository.board.BoardHashtagRepository;
import com.example.demo.service.board.BoardLikeService;
import com.example.demo.service.board.BoardSearchService;
import com.example.demo.service.board.BoardService;
import com.example.demo.service.member.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "게시판 (Board) API",description = "게시글 생성, 조회 , 수정, 삭제 및 커서 기반 목록 조회 기능을 제공")
@Log4j2
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardRestController {

    private final BoardService boardService;
    private final BoardLikeService boardLikeService;
    private final BoardHashtagRepository boardHashtagRepository;
    private final BoardSearchService boardSearchService;

    // ⭐ 1. 새로운 인증 상태 확인 API 추가
//    @Operation(summary = "로그인 상태 및 닉네임 확인", description = "유효한 JWT 쿠키가 있으면 사용자 ID와 닉네임을 반환합니다.")
//    @ApiResponse(responseCode = "200", description = "인증 성공, 사용자 정보 반환")
//    @ApiResponse(responseCode = "401", description = "인증 실패, 유효한 토큰 없음")
//    @GetMapping("/auth-check")
//    public ResponseEntity<Map<String, Object>> authCheck(@AuthenticationPrincipal CustomUserDetails details) {
//
//        // CustomUserDetails가 null이 아니라는 것은 Spring Security Filter를 통과했다는 의미
//        if (details == null) {
//            // 이 코드가 실행되는 경우는 드물지만, 안전 장치로 남겨둡니다.
//            log.info("인증 체크 요청: CustomUserDetails가 Null입니다.");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        Map<String, Object> response = new HashMap<>();
//
//        // Spring Security는 JWT의 Claims에서 정보를 추출하여 CustomUserDetails에 담습니다.
//        response.put("isLoggedIn", true);
//        response.put("memberId", details.getMemberId());
//        // JWT Payload에 저장된 닉네임 정보를 그대로 사용합니다.
//        response.put("userNickname", details.getNickname());
//
//        log.info("인증 체크 성공: MemberId={}, Nickname={}", details.getMemberId(), details.getNickname());
//
//        return ResponseEntity.ok(response);
//    }

    @Data
    private static class MarkdownPreviewRequest{
        @Schema(description = "미리보기할 마크다운 텍스트",example = "# 제목\n\n- 목록")
        private String markdownText;
    }

    private Long getCurrentMemberId(@AuthenticationPrincipal CustomUserDetails details){

        return details != null ? details.getMemberId() : null;
    }

    @Operation(summary = "마크다운 미리보기", description = "입력된 마크다운 텍스트를 HTML로 변환하여 반환합니다.")
    @ApiResponse(responseCode = "200",description = "HTML 컨텐츠 변환 성공",
                content = @Content(mediaType = "text/html",schema = @Schema(implementation = String.class)))
    @PostMapping(value = "/markdown-preview",produces = "text/html; charset=utf-8")
    public ResponseEntity<String> previewMarkdown(@RequestBody MarkdownPreviewRequest request){
//        String htmlContent = boardService.markdownHtml(request.getMarkdownText());
        return ResponseEntity.ok(boardService.markdownHtml(request.getMarkdownText()));
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file")MultipartFile file){
//        try{
//            String fileUrl = boardService.uploadFile(file);
//            return ResponseEntity.ok(fileUrl);
//        }
//        catch (IllegalArgumentException e){
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
        return ResponseEntity.ok(boardService.uploadFile(file));
    }

    @PostMapping
    public ResponseEntity<Long> createBoard(@Valid @RequestBody BoardRequestDTO boardRequestDTO, @AuthenticationPrincipal CustomUserDetails details){

//        if(details == null){
//            log.warn("게시글 작성 요청 : 비로그인 사용자 접근 거부");
//            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//        }
//
//        // details가 null이 아님을 확인했으므로, 안전하게 getMemberId()를 호출할 수 있습니다.
//        Long currentMemberId = details.getMemberId();
//
//        // 만약 memberId가 DB에 null로 저장된 특이 케이스까지 막고 싶다면 추가 검사
//        if (currentMemberId == null) {
//            log.warn("인증된 사용자이지만 MemberId가 누락되었습니다.");
//            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//        }
//
//        try{
//            Long boardId = boardService.saveNewBoard(boardRequestDTO, currentMemberId);
//            return new ResponseEntity<>(boardId,HttpStatus.CREATED);
//        }
//        catch(EntityNotFoundException e){
//           /* log.error("작성자 id : {} 를 찾을 수 없습니다."*//*,currentMemberId*//*);*/
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
        Long boardId = boardService.saveNewBoard(boardRequestDTO, details.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(boardId);

    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailResponse> detail(@PathVariable("boardId")Long boardId, @AuthenticationPrincipal CustomUserDetails details
                                                        ,HttpServletRequest request){

        Long currentMemberId = (details != null)?details.getMemberId(): null;

        BoardEntity board = boardService.findBoardByIdExceptUser(boardId);

        String clientIdentifier = (currentMemberId != null) ? String.valueOf(currentMemberId) : request.getRemoteAddr();

        boardService.increaseView(boardId, clientIdentifier);

//        Long authorId = board.getMember().getMemberId();
        boolean isAuthor = (currentMemberId != null) && currentMemberId.equals(board.getMember().getMemberId());

        Boolean isLiked = currentMemberId != null && boardLikeService.isBoardLikedByUser(boardId, currentMemberId);
//        List<BoardHashtagEntity> hashtagEntities = boardHashtagRepository.findAllByBoardId(boardId);
        List<String> tags = boardHashtagRepository.findAllByBoardId(boardId).stream()
                .map(entity -> entity.getHashtag().getName())
                .toList();
//        BoardDetailResponse response = BoardDetailResponse.of(board,isAuthor,isLiked,tags);
        return ResponseEntity.ok(BoardDetailResponse.of(board,isAuthor,isLiked,tags));
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<String> updateBoard(@PathVariable Long boardId, @Valid @RequestBody BoardRequestDTO boardRequestDTO, @AuthenticationPrincipal CustomUserDetails details) throws AccessDeniedException {

//        Long currentMemberId = details.getMemberId();
//        try{
//            boardRequestDTO.setBoardId(boardId);
//            // Service 메서드 시그니처 변경에 맞춰 currentMemberId를 추가했습니다.
//            boardService.updateBoard(boardRequestDTO, currentMemberId);
//            return ResponseEntity.noContent().build();
//        }
//        // AccessDeniedException은 서비스 계층에서 권한이 없는 경우 발생합니다.
//        catch (AccessDeniedException e) {
//            log.warn("게시글 수정 권한 없음 (403 Forbidden) : {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        }
//        catch (IllegalArgumentException e){
//            log.info("게시글 수정 실패 (404 Not Found) :{}", e.getMessage());
//            return ResponseEntity.notFound().build();
//        }
//        catch (Exception e){
//            log.error("게시글 수정 중 오류(500 Internal Server Error) :{}",e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
        boardRequestDTO.setBoardId(boardId);
        boardService.updateBoard(boardRequestDTO, details.getMemberId());
        return ResponseEntity.noContent().build();

    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardId,@AuthenticationPrincipal CustomUserDetails details) throws java.nio.file.AccessDeniedException {
        Long currentMemberId = details.getMemberId();

        boardService.deleteBoard(boardId,currentMemberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cursor")
    public ResponseEntity<BoardListResponse> getBoardsByCursor(
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false,value = "cursorId")Long cursorId,
            @RequestParam(required = false,value = "cursorDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime cursorDate,
            @AuthenticationPrincipal CustomUserDetails details)
    {
        Long currentMemberId =(details != null) ? details.getMemberId() : null;

//        try{
//            BoardListResponse response = boardService.getBoardsWithCursor(size,cursorId,cursorDate,currentMemberId);
//            return new ResponseEntity<>(response,HttpStatus.OK);
//        }
//        catch(Exception e){
//            log.error("커서 기반 게시글 조회 중 오류 발생 : {}",e.getMessage());
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
        return ResponseEntity.ok(boardService.getBoardsWithCursor(size,cursorId,cursorDate,currentMemberId));

    }
    @Operation(summary = "게시글 좋아요/취소 토글",description = "인증된 사용자가 특정 게시글에 좋아요를 누르거나 취소합니다.")
    @ApiResponse(responseCode = "200",description = "좋아요 상태 변경 성공. body:true(좋아요), false(취소)")
    @ApiResponse(responseCode = "401",description = "인증 필요( 토큰 없음)")
    @ApiResponse(responseCode = "404",description = "게시글 또는 회원 ID 찾을 수 없음")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/{boardId}/like")
    public ResponseEntity<Boolean> toggleLike(@PathVariable("boardId")Long boardId,
                                              @AuthenticationPrincipal CustomUserDetails details){
//        Long currentMemberId = getCurrentMemberId(details);

//        if(currentMemberId == null){
//            log.warn("좋아요 요청 : 비로그인 사용자 접근 거부 (401)");
//            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//        }
//
//        try{
//            Boolean isLiked = boardLikeService.toggleLike(boardId,currentMemberId);
//
//            return ResponseEntity.ok(isLiked);
//        }
//        catch (EntityNotFoundException e){
//            log.warn("좋아요 토글 실패 (404 Not Found) : {}",e.getMessage());
//            return ResponseEntity.notFound().build();
//        }
//        catch (Exception e){
//            log.error("좋아요 토글 중 오류 (500 Internal Server Error) : {}",e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
        return ResponseEntity.ok(boardLikeService.toggleLike(boardId, details.getMemberId()));

    }

    @GetMapping("/search")
    public ResponseEntity<List<BoardResponse>> searchBoards(
            @RequestParam(value = "keyword",required = false) String keyword,
            @RequestParam(required = false,value = "tag") String tagName,
            @RequestParam(required = false) Long lastBoardId,
            @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        if(keyword != null && keyword.length() >100){
            return ResponseEntity.badRequest().build();
        }
        if(tagName != null && tagName.length() >50){
            return ResponseEntity.badRequest().build();
        }

        Long currentMemberId = (userDetails != null) ? userDetails.getMemberId() : null;
//        int limit = 20;
//        List<BoardResponse> result = boardService.searchBoards(keyword,tagName,currentMemberId,lastBoardId);
        return ResponseEntity.ok(boardService.searchBoards(keyword,tagName,currentMemberId,lastBoardId));
    }

    @Operation(summary = "ES bulk indexing", description = "기존 DB 데이터를 Elasticsearch에 전체 색인합니다.")
    @PostMapping("/admin/es-bulk-index")
    public ResponseEntity<Map<String,String>> bulkIndex() {
//        try {
            boardSearchService.bulkIndex();
//            return ResponseEntity.ok("ES bulk indexing 완료");
//        } catch (Exception e) {
//            log.error("[ES] bulk indexing 실패: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("bulk indexing 실패: " + e.getMessage());
//        }
        return ResponseEntity.ok(Map.of("message","ES bulk indexing 완료"));
    }
}
