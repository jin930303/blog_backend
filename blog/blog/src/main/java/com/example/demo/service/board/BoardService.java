package com.example.demo.service.board;

import com.example.demo.dto.board.BoardDTO;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.entity.board.BoardEntity;

import java.util.List;

public interface BoardService {
    Long saveNewBoard(BoardDTO boardDTO);

    List<BoardResponse> findAllBoards();

    BoardResponse findBoardById(Long id);

    String markdownHtml(String markdownText);
}
