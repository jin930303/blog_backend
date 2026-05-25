package com.example.demo.controller.board;

import com.example.demo.dto.board.CommentRequestDTO;
import com.example.demo.dto.board.CommentResponseDTO;
import com.example.demo.service.board.BoardCommentService;
import com.example.demo.service.member.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/boards/{boardId}/comments")
@Slf4j
@RequiredArgsConstructor
public class BoardCommentController {

    private final BoardCommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentResponseDTO>> getComments (@PathVariable Long boardId){
        log.info("댓글 목록 조회 요청 - boardId : {}",boardId);

//        List<CommentResponseDTO> comments = commentService.getCommentList(boardId);
        return ResponseEntity.ok(commentService.getCommentList(boardId));
    }

    @PostMapping
    public ResponseEntity<CommentResponseDTO> createComment(
            @PathVariable Long boardId , @Valid @RequestBody CommentRequestDTO requestDTO){

        log.info("댓글 작성 요청- boardId :{}, content : {}",boardId,requestDTO.getContent());
//        try{
//            CommentResponseDTO response = commentService.createComment(boardId,requestDTO);
//            return ResponseEntity.status(HttpStatus.CREATED).body(response);
//        } catch (IllegalStateException e){
//            log.error("댓글 작성 실패 - 인증 오류 : {}",e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        } catch (IllegalArgumentException e){
//            log.error("댓글 작성 실패 - 잘못된 요청 : {}",e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(boardId,requestDTO));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId ,
            @Valid @RequestBody CommentRequestDTO requestDTO){

        log.info("댓글 수정 요청 - boardId : {}, commentId : {}",boardId,commentId);
//        try{
//            CommentResponseDTO response = commentService.updateComment(boardId,commentId,requestDTO);
//            return ResponseEntity.ok(response);
//        } catch (IllegalStateException e){
//            log.error("댓글 수정 실패 - 권한 오류 : {}",e.getMessage());
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        } catch (IllegalArgumentException e){
//            log.error("댓글 수정 실패 - 잘못된 요청 : {}",e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
        return ResponseEntity.ok(commentService.updateComment(boardId,commentId,requestDTO));

    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long boardId,
                                              @PathVariable Long commentId){
        log.info("댓글 삭제 요청 - boardId :{} commentId :{}",boardId,commentId);
//        try{
//            commentService.deleteComment(boardId,commentId);
//            return ResponseEntity.noContent().build();
//        } catch (IllegalStateException e){
//            log.error("댓글 삭제 실패 - 권한 오류 : {}",e.getMessage() );
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        } catch (IllegalArgumentException e){
//            log.error("댓글 삭제 실패 - 잘못된 요청 : {}",e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
        commentService.deleteComment(boardId,commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<Boolean> toggleLike(@PathVariable Long boardId, @PathVariable Long commentId){
        log.info("댓글 좋아요 토글 요청 - boardId : {} commentId : {}",boardId,commentId);
//        try{
//            Boolean isLiked = commentService.toggleLike(boardId,commentId);
//            return ResponseEntity.ok(isLiked);
//        } catch (IllegalStateException e){
//            log.error("좋아요 실패 - 인증 오류 : {}",e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        } catch (IllegalArgumentException e){
//            log.error("좋아요 실패 - 잘못된 요청 : {}",e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
        return ResponseEntity.ok(commentService.toggleLike(boardId,commentId));
    }

}
