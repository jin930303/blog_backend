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

    @Query("SELECT b FROM BoardEntity b " +
            "LEFT JOIN FETCH b.member m " +
            "LEFT JOIN b.boardHashtags bh " +
            "LEFT JOIN bh.hashtag h " +
            "WHERE (:keyword IS NULL OR b.title LIKE CONCAT('%' , :keyword , '%') OR b.content LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:tagName IS NULL OR h.name = :tagName) " +
            "ORDER BY b.inputDate DESC")
    List<BoardEntity> searchBoards(@Param("keyword") String keyword, @Param("tagName") String tagName);



    @Modifying
    @Query("UPDATE BoardEntity b SET b.views = b.views + :increment WHERE b.boardId = :boardId")
    void addViews(@Param("boardId") long boardId, @Param("increment") long increment);
}
