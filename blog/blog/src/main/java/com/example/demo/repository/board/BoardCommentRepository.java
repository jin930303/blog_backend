package com.example.demo.repository.board;

import com.example.demo.entity.board.BoardCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardCommentRepository extends JpaRepository<BoardCommentEntity,Long> {

}
