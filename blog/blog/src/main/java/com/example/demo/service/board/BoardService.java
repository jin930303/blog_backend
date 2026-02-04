package com.example.demo.service.board;

import com.example.demo.dto.board.BoardRequestDTO;
import com.example.demo.dto.board.BoardListResponse;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.entity.board.BoardEntity;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

public interface BoardService {
    Long saveNewBoard(BoardRequestDTO boardRequestDTO, Long currentMemberId);

    List<BoardResponse> findAllBoards(Long currentMemberId);

    BoardResponse findBoardById(Long id, Long currentMemberId);

    String markdownHtml(String markdownText);

    String uploadFile(MultipartFile file);

    void updateBoard(BoardRequestDTO boardRequestDTO, Long currentMemberId) throws AccessDeniedException;

    void deleteBoard(Long boardId, Long currentMemberId) throws AccessDeniedException;

    void increaseView(Long boardId);

    BoardListResponse getBoardsWithCursor(int size, Long cursorId, LocalDateTime cursorDate,Long currentMemberId);

    BoardEntity findBoardByIdExceptUser(Long boardId);

    List<BoardResponse> searchBoards(String keyword, String tagName, Long currentMemberId);
}
