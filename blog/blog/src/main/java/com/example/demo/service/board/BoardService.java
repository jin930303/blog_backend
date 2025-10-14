package com.example.demo.service.board;

import com.example.demo.dto.board.BoardDTO;
import com.example.demo.dto.board.BoardResponse;

import java.util.List;

public interface BoardService {
    Long saveNewBoard(BoardDTO boardDTO);

    List<BoardResponse> findAllBoards();
}
