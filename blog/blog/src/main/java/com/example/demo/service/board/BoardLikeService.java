package com.example.demo.service.board;

public interface BoardLikeService {

    Boolean toggleLike(Long boardId, Long memberId);
}
