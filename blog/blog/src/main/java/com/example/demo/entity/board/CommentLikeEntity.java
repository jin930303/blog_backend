package com.example.demo.entity.board;

import com.example.demo.entity.member.MemberEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(name = "comment_like_seq",sequenceName = "comment_like_seq",allocationSize = 1)
@Table(name = "comment_like",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"comment_id","member_id"})
})
public class CommentLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "comment_like_seq")
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id",nullable = false)
    private BoardCommentEntity comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",nullable = false)
    private MemberEntity member;
}
