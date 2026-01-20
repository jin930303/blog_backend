package com.example.demo.repository.board;

import com.example.demo.entity.board.BoardCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardCommentRepository extends JpaRepository<BoardCommentEntity,Long> {


    @Query("SELECT c FROM BoardCommentEntity c " +
            "WHERE c.board.boardId = :boardId " +
            "AND c.deleted = false " +
            "ORDER BY c.inputDate DESC")
    List<BoardCommentEntity> findByBoardIdAndNotDeleted(@Param("boardId") Long boardId);

    @Query("SELECT c FROM BoardCommentEntity c " +
            "WHERE c.commentId = :commentId " +
            "AND c.deleted = false")
    Optional<BoardCommentEntity> findByCommentIdAndDeletedFalse(@Param("commentId") Long commentId);
}
