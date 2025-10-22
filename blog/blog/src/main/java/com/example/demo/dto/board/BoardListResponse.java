package com.example.demo.dto.board;


import java.time.LocalDateTime;
import java.util.List;

public record BoardListResponse(
        List<BoardResponse> content,
        Boolean hasNext,
        Long nextCursorId,
        LocalDateTime nextCursorDate
) {


}
