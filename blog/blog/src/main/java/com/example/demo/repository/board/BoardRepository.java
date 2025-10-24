package com.example.demo.repository.board;

import com.example.demo.entity.board.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity,Long> {
    @Modifying
    @Query(value = "update board set views = views + 1 where board_id =  :boardId",nativeQuery = true)
    void increaseView(@Param("boardId") Long boardId);


    @Query(value = """
        SELECT b FROM BoardEntity b 
        WHERE (:cursorId IS NULL) OR 
              (b.inputDate < :cursorDate) OR 
              (b.inputDate = :cursorDate AND b.boardId < :cursorId)
        ORDER BY b.inputDate DESC, b.boardId DESC
    """)
    List<BoardEntity> findNextBoards(@Param("size") int size, @Param("cursorId") Long cursorId, @Param("cursorDate") LocalDateTime cursorDate);
}
