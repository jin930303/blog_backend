package com.example.demo.entity.board;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "HASHTAG")
@SequenceGenerator(name = "hashtag_seq",sequenceName = "SEQ_HASHTAG_ID",initialValue = 1,allocationSize = 1)
public class HashtagEntity {

    @Id
    @Column(name = "hashtag_id")
    @GeneratedValue(generator = "hashtag_seq",strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false,unique = true,length = 50)
    private String name;

    @Builder
    public HashtagEntity(String name){
        this.name=name;
    }

}
