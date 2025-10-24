package com.example.demo.service.board;

import com.example.demo.dto.board.BoardDTO;
import com.example.demo.dto.board.BoardListResponse;
import com.example.demo.dto.board.BoardResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface BoardService {
    Long saveNewBoard(BoardDTO boardDTO);

    List<BoardResponse> findAllBoards();

    BoardResponse findBoardById(Long id);

    String markdownHtml(String markdownText);

    String uploadFile(MultipartFile file);

    void updateBoard(BoardDTO boardDTO);

    void deleteBoard(Long boardId);

    void increaseView(Long boardId);

    BoardListResponse getBoardsWithCursor(int size, Long cursorId, LocalDateTime cursorDate);
}
