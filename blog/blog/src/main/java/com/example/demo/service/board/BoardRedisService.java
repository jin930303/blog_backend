package com.example.demo.service.board;

public interface BoardRedisService {
    void increaseViewCount(Long boardId, String clientIdentifier);
}
