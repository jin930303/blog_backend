package com.example.demo.controller.board;

import com.example.demo.service.board.BoardRedisService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ViewCountConcurrencyTest {

    @Autowired
    private BoardRedisService boardRedisService;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @AfterEach
    void tearDown(){
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushAll();
            return null;
        });
    }

    @Test
    @DisplayName("100명이 동시에 조회했을 때 조회수가 100개 올라가야 한다.")
    void view_count_concurrency_test() throws InterruptedException {

        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch latch = new CountDownLatch(threadCount);

        Long boarId = 1L;

        for(int i = 0; i<threadCount; i++){
            String userIp = "192.168.0."+i;

            executorService.submit(()->{
                try{
                    boardRedisService.increaseViewCount(boarId,userIp);
                }finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        String countKey = "view:count:"+boarId;
        String viewCount = redisTemplate.opsForValue().get(countKey);

        System.out.println("최종 조회수:"+viewCount);

        assertThat(viewCount).isNotNull();
        assertThat(Integer.parseInt(viewCount)).isEqualTo(threadCount);
    }
}
