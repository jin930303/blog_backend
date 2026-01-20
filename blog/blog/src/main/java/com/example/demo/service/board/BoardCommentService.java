package com.example.demo.service.board;

import com.example.demo.dto.board.CommentRequestDTO;
import com.example.demo.dto.board.CommentResponseDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface BoardCommentService {
    List<CommentResponseDTO> getCommentList(Long boardId);

    CommentResponseDTO updateComment(Long boardId, Long commentId, @Valid CommentRequestDTO requestDTO);

    void deleteComment(Long boardId, Long commentId);

    Boolean toggleLike(Long boardId, Long commentId);

    CommentResponseDTO createComment(Long boardId, @Valid CommentRequestDTO requestDTO);
}
