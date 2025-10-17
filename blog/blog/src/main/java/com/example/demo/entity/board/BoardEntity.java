package com.example.demo.entity.board;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "board")
@SequenceGenerator(name = "board_seq",sequenceName = "board_seq",initialValue = 1,allocationSize = 1)
public class BoardEntity {
    @Id
    @Column(name = "board_id")
    @GeneratedValue(generator = "board_seq",strategy = GenerationType.SEQUENCE)
    private Long boardId;

    private String title;

    @Lob
    private String content;

    private String nickname;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_original_name")
    private String fileOriginalName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "input_date",updatable = false)
    private LocalDateTime inputDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    private int likes;
    private int views;
    private String category;


}
