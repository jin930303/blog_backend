package com.example.demo.controller.board;

import com.example.demo.dto.board.BoardDTO;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.service.board.BoardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/boards")
public class BoardRestController {

    private final BoardService boardService;



    public BoardRestController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public ResponseEntity<String> createBoard(@ModelAttribute BoardDTO boardDTO){
        Long boardId = boardService.saveNewBoard(boardDTO);
        if(boardDTO.getImg()!=null && !boardDTO.getImg().isEmpty()){
            System.out.println("--- 파일 정보 처리 시작 ---");
            System.out.println("파일명: " + boardDTO.getImg().getOriginalFilename());
            System.out.println("파일 크기: " + boardDTO.getImg().getSize() + " bytes");
            System.out.println("--- 파일 정보 처리 완료 ---");
        }
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
