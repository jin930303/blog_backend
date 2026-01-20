package com.example.demo.repository.board;

import com.example.demo.entity.board.BoardCommentEntity;
import com.example.demo.entity.board.CommentLikeEntity;
import com.example.demo.entity.member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CommentLikeRepository  extends JpaRepository<CommentLikeEntity,Long> {
    
    Optional<CommentLikeEntity> findByCommentAndMember(BoardCommentEntity comment, MemberEntity member);

    long countByCommentAndMember(BoardCommentEntity comment, MemberEntity currentMember);
}
