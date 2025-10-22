package com.example.demo.controller.board;

import com.example.demo.dto.board.BoardDTO;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.entity.board.BoardEntity;
import com.example.demo.service.board.BoardService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.DeclareError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardRestController {

    private final BoardService boardService;

    @Data
    private static class MarkdownPreviewRequest{
        private String markdownText;
    }


    @PostMapping(value = "/markdown-preview",produces = "text/html; charset=utf-8")
    public ResponseEntity<String> previewMarkdown(@RequestBody MarkdownPreviewRequest request){
        String htmlContent = boardService.markdownHtml(request.getMarkdownText());
        return ResponseEntity.ok(htmlContent);
    }

    @PostMapping("upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file")MultipartFile file){
        try{
            String fileUrl = boardService.uploadFile(file);
            return ResponseEntity.ok(fileUrl);
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping
    public ResponseEntity<String> createBoard(@RequestBody BoardDTO boardDTO){
        Long boardId = boardService.saveNewBoard(boardDTO);
        log.info("게시글이 성공적으로 작성되었습니다. id : {}",boardId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<BoardResponse>> getBoardList(){
        List<BoardResponse> list = boardService.findAllBoards();
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> detail(@PathVariable("boardId")Long boardId){
        BoardResponse board = boardService.findBoardById(boardId);
        System.out.println("boardId : "+boardId);
        return ResponseEntity.ok(board);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<String> updateBoard(@PathVariable Long boardId, @RequestBody BoardDTO boardDTO){
        try{
            boardDTO.setBoardId(boardId);
            boardService.updateBoard(boardDTO);
            return ResponseEntity.noContent().build();
        }
        catch (IllegalArgumentException e){
            log.info("게시글 수정 실패 (404) :{}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e){
            log.info("게시글 수정 중 오류(500) :{}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardId){
        boardService.deleteBoard(boardId);
        return ResponseEntity.noContent().build();
    }
}
