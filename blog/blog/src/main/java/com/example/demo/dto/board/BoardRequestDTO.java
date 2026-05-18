package com.example.demo.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.crypto.Mac;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class BoardRequestDTO {
        private Long boardId;

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200,message = "제목은 200자를 초과할 수 없습니다.")
        private String title;

        @Size(max = 50000,message = "내용이 너무 깁니다.")
        private String content;

        @Size(max = 50,message = "닉네임은 50자를 초과할 수 없습니다.")
        private String nickname;

        @Size(max = 50,message = "카테고리는 50자를 초과할 수 없습니다.")
        private String category;

        @Size(max = 10,message = "태그는 최대 10개까지 가능합니다.")
        private List<@Size(max = 30,message = "태그는 30자를 초과할 수 없습니다.") String> tags;
    }



