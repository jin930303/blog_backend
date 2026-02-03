package com.example.demo.repository.board;

import com.example.demo.entity.board.BoardHashtagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardHashtagRepository extends JpaRepository<BoardHashtagEntity,Long> {

    @Modifying
    @Query("DELETE FROM BoardHashtagEntity bh WHERE bh.board.boardId = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);

    @Query("SELECT bh FROM BoardHashtagEntity bh WHERE bh.board.boardId = :boardId")
    List<BoardHashtagEntity> findAllByBoardId(@Param("boardId") Long boardId);
}
