package com.example.demo.repository.board;

import com.example.demo.entity.board.BoardLikeEntity;
import jakarta.persistence.Column;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLikeEntity,Long> {



    @Query("select b1 from BoardLikeEntity b1 where b1.member.memberId = :memberId and b1.board.boardId = :boardId")
    Optional<BoardLikeEntity> findByMemberIdAndBoardId(@Param("memberId") Long memberId,@Param("boardId") Long boardId);

    @Transactional
    @Modifying
    @Query(value = "delete from board_like where member_id = :memberId and board_id = :boardId",nativeQuery = true)
    void deleteByMemberIdAndBoardId(@Param("memberId") Long memberId,@Param("boardId") Long boardId);

}
