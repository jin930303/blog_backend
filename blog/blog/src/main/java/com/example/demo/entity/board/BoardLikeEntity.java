package com.example.demo.entity.board;

import com.example.demo.entity.member.MemberEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "board_like",uniqueConstraints = {
        @UniqueConstraint(
                name = "UC_MEMBER_BOARD",
                columnNames = {"member_id","board_id"}
        )
})
@ToString(exclude = {"member","board"})
@SequenceGenerator(name = "board_like_seq",sequenceName = "board_like_seq",allocationSize = 1)
public class BoardLikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "board_like_seq")
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",nullable = false)
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id",nullable = false)
    private BoardEntity board;

    @Column(updatable = false)
    private LocalDateTime likeDate;

    @PrePersist
    public void prePersist(){
        this.likeDate = LocalDateTime.now();
    }
}
