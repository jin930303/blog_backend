package com.example.demo.entity.board;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "BOARD_HASHTAG",uniqueConstraints = {@UniqueConstraint(columnNames = {"board_id","hashtag_id"})})
@SequenceGenerator(name = "board_hashtag_seq",sequenceName = "SEQ_BOARD_HASHTAG_ID",allocationSize = 1,initialValue = 1)
public class BoardHashtagEntity {

    @Id
    @GeneratedValue(generator = "board_hashtag_seq",strategy = GenerationType.SEQUENCE)
    @Column(name = "map_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id",nullable = false)
    private BoardEntity board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id",nullable = false)
    private HashtagEntity hashtag;

    @Builder
    public BoardHashtagEntity(BoardEntity board, HashtagEntity hashtag){
        this.board = board;
        this.hashtag = hashtag;
    }
}
