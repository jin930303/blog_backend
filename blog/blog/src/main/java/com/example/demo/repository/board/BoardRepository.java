package com.example.demo.repository.board;

import com.example.demo.dto.board.BoardResponse;
import com.example.demo.entity.board.BoardEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity,Long> {

    @Query(value = "SELECT b FROM board b JOIN FETCH b.member ORDER BY b.board_id DESC",nativeQuery = true)
    List<BoardEntity> findAllWithMember();

//    @Modifying
//    @Query(value = "update board set views = views + 1 where board_id =  :boardId",nativeQuery = true)
//    void increaseView(@Param("boardId") Long boardId);


    @Query(value = """
        SELECT * FROM (
            SELECT 
                b.board_id AS boardId, 
                b.title, 
                b.content_summary AS contentSummary, 
                m.nickname, 
                b.file_path AS filePath, 
                b.file_original_name AS fileOriginalName, 
                b.file_size AS fileSize, 
                b.input_date AS inputDate, 
                b.modified_date AS modifiedDate, 
                b.views, 
                b.likes, 
                b.category
            FROM board b
            JOIN member m ON b.member_id = m.member_id
            WHERE (:cursorDate IS NULL OR b.input_date < :cursorDate OR (b.input_date = :cursorDate AND b.board_id < :cursorId))
            ORDER BY b.input_date DESC, b.board_id DESC
        ) WHERE ROWNUM <= :limitSize
    """, nativeQuery = true)
    List<BoardSummary> findBoardListNative(@Param("cursorId") Long cursorId,
                                           @Param("cursorDate") LocalDateTime cursorDate,
                                           @Param("limitSize") int limitSize);

    @Query(value = "SELECT board_id, title, content_summary, nickname, " +
            "file_path, file_original_name, file_size, " +
            "input_date, modified_date, likes, views, category, member_id " +
            "FROM ( " +
            "SELECT DISTINCT b.board_id, b.title, b.content_summary, b.nickname, " +
            "b.file_path, b.file_original_name, b.file_size, " +
            "b.input_date, b.modified_date, b.likes, b.views, b.category, b.member_id " +
            "FROM board b " +
            "LEFT JOIN board_hashtag bh ON b.board_id = bh.board_id " +
            "LEFT JOIN hashtag h ON bh.hashtag_id = h.hashtag_id " +
            "WHERE (:keyword IS NULL OR b.title LIKE :keyword OR b.content_summary LIKE :keyword) " +
            "AND (:tagName IS NULL OR h.name = :tagName) " +
            "AND (:lastBoardId IS NULL OR b.board_id < :lastBoardId) " +
            "ORDER BY b.board_id DESC " +
            ") WHERE ROWNUM <= :size",
            nativeQuery = true)
    List<Object[]> searchBoards(@Param("keyword") String keyword, @Param("tagName") String tagName, @Param("lastBoardId")Long lastBoardId,int size);



    @Modifying
    @Query("UPDATE BoardEntity b SET b.views = b.views + :increment WHERE b.boardId = :boardId")
    void addViews(@Param("boardId") long boardId, @Param("increment") long increment);
}
