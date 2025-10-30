package com.example.demo.repository.board;

import com.example.demo.entity.board.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity,Long> {

    @Query(value = "SELECT b FROM board b JOIN FETCH b.member ORDER BY b.board_id DESC",nativeQuery = true)
    List<BoardEntity> findAllWithMember();

    @Modifying
    @Query(value = "update board set views = views + 1 where board_id =  :boardId",nativeQuery = true)
    void increaseView(@Param("boardId") Long boardId);


    @Query("SELECT b FROM BoardEntity b JOIN FETCH b.member " +
            "WHERE (:cursorId IS NULL OR b.boardId < :cursorId) AND b.inputDate <= :cursorDate " +
            "ORDER BY b.inputDate DESC, b.boardId DESC")
    List<BoardEntity> findNextBoardsWithMember(@Param("cursorId") Long cursorId, @Param("cursorDate") LocalDateTime cursorDate, @Param("pageSize")int pageSize);


}
