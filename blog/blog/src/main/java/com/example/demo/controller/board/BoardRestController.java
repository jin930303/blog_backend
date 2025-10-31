package com.example.demo.controller.board;

import com.example.demo.dto.board.BoardDTO;
import com.example.demo.dto.board.BoardListResponse;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.service.board.BoardService;
import com.example.demo.service.member.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
@Tag(name = "게시판 (Board) API",description = "게시글 생성, 조회 , 수정, 삭제 및 커서 기반 목록 조회 기능을 제공")
@Log4j2
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardRestController {

    private final BoardService boardService;

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
        String htmlContent = boardService.markdownHtml(request.getMarkdownText());
        return ResponseEntity.ok(htmlContent);
    }

//    @PostMapping("upload-image")
//    public ResponseEntity<?> uploadImage(@RequestParam("file")MultipartFile file){
//        try{
//            String fileUrl = boardService.uploadFile(file);
//            return ResponseEntity.ok(fileUrl);
//        }
//        catch (IllegalArgumentException e){
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
    @PostMapping
    public ResponseEntity<Long> createBoard(@RequestBody BoardDTO boardDTO, @AuthenticationPrincipal CustomUserDetails details){

        if(details == null){
            log.warn("게시글 작성 요청 : 비로그인 사용자 접근 거부");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // details가 null이 아님을 확인했으므로, 안전하게 getMemberId()를 호출할 수 있습니다.
        Long currentMemberId = details.getMemberId();

        // 만약 memberId가 DB에 null로 저장된 특이 케이스까지 막고 싶다면 추가 검사
        if (currentMemberId == null) {
            log.warn("인증된 사용자이지만 MemberId가 누락되었습니다.");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try{
            Long boardId = boardService.saveNewBoard(boardDTO, currentMemberId);
            return new ResponseEntity<>(boardId,HttpStatus.CREATED);
        }
        catch(EntityNotFoundException e){
           /* log.error("작성자 id : {} 를 찾을 수 없습니다."*//*,currentMemberId*//*);*/
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> detail(@PathVariable("boardId")Long boardId,@AuthenticationPrincipal CustomUserDetails details){

        Long currentMemberId = details.getMemberId();

        BoardResponse board = boardService.findBoardById(boardId,currentMemberId);
        boardService.increaseView(boardId);
        log.info("boardId = {}",boardId);
        log.info("content = {}",board.content());
        System.out.println("boardId : "+boardId);
        return ResponseEntity.ok(board);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<String> updateBoard(@PathVariable Long boardId, @RequestBody BoardDTO boardDTO,@AuthenticationPrincipal CustomUserDetails details){

        Long currentMemberId = details.getMemberId();


        try{
            boardDTO.setBoardId(boardId);
            // Service 메서드 시그니처 변경에 맞춰 currentMemberId를 추가했습니다.
            boardService.updateBoard(boardDTO, currentMemberId);
            return ResponseEntity.noContent().build();
        }
        // AccessDeniedException은 서비스 계층에서 권한이 없는 경우 발생합니다.
        catch (AccessDeniedException e) {
            log.warn("게시글 수정 권한 없음 (403 Forbidden) : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
        catch (IllegalArgumentException e){
            log.info("게시글 수정 실패 (404 Not Found) :{}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e){
            log.error("게시글 수정 중 오류(500 Internal Server Error) :{}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardId,@AuthenticationPrincipal CustomUserDetails details) throws java.nio.file.AccessDeniedException {
        Long currentMemberId = details.getMemberId();

        boardService.deleteBoard(boardId,currentMemberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cursor")
    public ResponseEntity<BoardListResponse> getBoardsByCursor(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false,value = "cursorId")Long cursorId,
            @RequestParam(required = false,value = "CursorDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime cursorDate,
            @AuthenticationPrincipal CustomUserDetails details)
    {
        Long currentMemberId =(details != null) ? details.getMemberId() : null;

        try{
            BoardListResponse response = boardService.getBoardsWithCursor(size,cursorId,cursorDate,currentMemberId);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }
        catch(Exception e){
            log.error("커서 기반 게시글 조회 중 오류 발생 : {}",e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
