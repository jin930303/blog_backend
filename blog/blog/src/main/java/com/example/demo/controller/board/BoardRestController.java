package com.example.demo.controller.board;

import com.example.demo.dto.board.BoardDTO;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.service.board.BoardService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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

    @PostMapping
    public ResponseEntity<String> createBoard(@ModelAttribute BoardDTO boardDTO){
        Long boardId = boardService.saveNewBoard(boardDTO);
//
        return ResponseEntity.status(HttpStatus.CREATED).body("게시글이 성공적으로 작성되었습니다. ID : "+ boardId);
    }

    @GetMapping
    public ResponseEntity<List<BoardResponse>> getBoardList(){
        List<BoardResponse> list = boardService.findAllBoards();
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> detail(@PathVariable("id")Long id){
        BoardResponse board = boardService.findBoardById(id);
        System.out.println("id : "+id);
        return ResponseEntity.ok(board);
    }
}
