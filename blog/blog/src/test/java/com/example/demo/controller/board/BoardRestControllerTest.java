package com.example.demo.controller.board;

import com.example.demo.entity.board.BoardEntity;
import com.example.demo.entity.member.MemberEntity;
import com.example.demo.repository.board.BoardRepository;
import com.example.demo.repository.member.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
class BoardBulkInsertTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager entityManager; // 10만 개 처리를 위해 영속성 컨텍스트 직접 관리

    @Test
    @Commit
    @Transactional
    public void createLargeDummyData() {
        final int TOTAL_COUNT = 100000; // 10만 개
        final int BATCH_SIZE = 1000;    // 1000개씩 끊어서 저장

        System.out.println("--- 🚀 대용량(10만 건) 데이터 생성을 시작합니다 ---");

        // 1. 작성자 회원 조회
        MemberEntity author = memberRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("DB에 회원이 최소 1명 있어야 합니다."));

        Random random = new Random();
        List<BoardEntity> batchList = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= TOTAL_COUNT; i++) {
            BoardEntity entity = new BoardEntity();

            // 제목 & 카테고리
            entity.setTitle(String.format("대용량 테스트 게시글 - %06d", i));
            entity.setCategory("free"); // 필수값

            // 본문 (썸네일용 이미지 태그 포함)
            // picsum 이미지는 id를 랜덤으로 돌려 다양하게 나오게 함
            int imageId = (i % 100) + 1;
            String imageUrl = String.format("https://picsum.photos/id/%d/600/400", imageId);

            String content = String.format(
                    "<p>이것은 %d번째 더미 데이터입니다.</p>" +
                            "<img src='%s' alt='sample-image' />" +
                            "<p>성능 테스트를 위한 본문 내용입니다. 데이터가 많아도 리스트가 잘 뜨는지 확인해보세요.</p>",
                    i, imageUrl
            );
            entity.setContent(content);

            // 작성자, 조회수, 좋아요
            entity.setMember(author);
            entity.setNickname(author.getNickname());
            entity.setViews(random.nextInt(5000)); // 0~5000 조회수
            entity.setLikes(random.nextInt(500));  // 0~500 좋아요

            // 날짜 (최신순 정렬 테스트용)
            // 1분씩 차이를 두어 10만 개가 시간 순서대로 쌓이게 함 (약 2달치 데이터)
            entity.setInputDate(LocalDateTime.now().minusMinutes(TOTAL_COUNT - i));

            // 리스트에 담기
            batchList.add(entity);

            // 2. 1000개가 찰 때마다 DB에 저장하고 메모리 비우기
            if (i % BATCH_SIZE == 0) {
                boardRepository.saveAll(batchList); // 배치 저장
                boardRepository.flush();            // DB 반영
                entityManager.clear();              // ⭐ 메모리(1차 캐시) 비우기 (OOM 방지)
                batchList.clear();                  // 리스트 초기화

                System.out.printf("--- %d건 저장 완료 (진행률: %.1f%%) ---\n", i, (double)i/TOTAL_COUNT * 100);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("--- ✅ 10만 건 저장 완료! 걸린 시간: " + (endTime - startTime) / 1000 + "초 ---");
    }
}