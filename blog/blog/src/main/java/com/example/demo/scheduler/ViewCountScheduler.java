package com.example.demo.scheduler;

import com.example.demo.repository.board.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final RedisTemplate<String,String> redisTemplate;
    private final BoardRepository boardRepository;

    @Scheduled(fixedDelay = 180000)
    @Transactional
    public void syncViewCounts(){
        Set<String> boardIds = redisTemplate.opsForSet().members("view:changed_boards");

        if(boardIds == null || boardIds.isEmpty()){
            return;
        }

        log.info("조회수 동기화 시작 : 총 {}건",boardIds.size());

        for(String boardIdStr : boardIds){
            String countKey = "view:count:"+boardIdStr;
            String countVal = redisTemplate.opsForValue().get(countKey);

            if(countVal != null){
                long boardId = Long.parseLong(boardIdStr);
                long increment = Long.parseLong(countVal);

                boardRepository.addViews(boardId,increment);

                redisTemplate.delete(countKey);
                redisTemplate.opsForSet().remove("view:changed_boards",boardIdStr);
            }
        }
        log.info("조회수 동기화 완료");
    }
}
