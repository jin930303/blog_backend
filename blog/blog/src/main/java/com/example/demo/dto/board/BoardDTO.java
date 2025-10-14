package com.example.demo.dto.board;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@NoArgsConstructor
public class BoardDTO {

        private String title;
        private String content;
        private String nickname;
        private MultipartFile img;
        private String category;



    }



