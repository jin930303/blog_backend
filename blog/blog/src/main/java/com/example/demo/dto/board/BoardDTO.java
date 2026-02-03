package com.example.demo.dto.board;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class BoardDTO {
        private Long boardId;
        private String title;
        private String content;
        private String nickname;
        private String category;
        private List<String> tags;
    }



