package com.example.demo.service.board;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class BoardRedisServiceImpl implements BoardRedisService{

    private final RedisTemplate<String,String> redisTemplate;

    @Override
    public void increaseViewCount(Long boardId, String clientIdentifier) {
        // 중복 조회 방지 키
        String logKey = "view:log:" + boardId+ ":" + clientIdentifier;

        // 조회수 카운트 키
        String countKey = "view:count:" + boardId;

        //중복 조회가 아니라면
        if(!redisTemplate.hasKey(logKey)){

            //조회수 증가
            redisTemplate.opsForValue().increment(countKey);

            //변경된 게시글 id를 Set에 저장(스케줄러 db 반영 위함)
            redisTemplate.opsForSet().add("view:changed_boards",String.valueOf(boardId));

            //중복 방지 로그 저장(24시간 유지, 하루에 1번 카운트)
            redisTemplate.opsForValue().set(logKey,"1", Duration.ofHours(24));
        }
    }
}
