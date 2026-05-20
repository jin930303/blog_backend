package com.example.demo.config;

import com.example.demo.document.BoardDocument;
import com.example.demo.service.board.BoardSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchInitializer implements ApplicationRunner {

    private final BoardSearchService boardSearchService;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void run(ApplicationArguments args){
        try{
            long count = elasticsearchOperations.count(Query.findAll(), BoardDocument.class);

            if(count == 0){
                log.info("[ES] 인덱스가 비어있어 bulk indexing 시작");
                boardSearchService.bulkIndex();
                log.info("[ES] bulk indexing 완료");
            } else {
                log.info("[ES] 기존 인덱스 유지 - {}건",count);
            }
        } catch (Exception e){
            log.error("[ES] 초기화 실패 - 서버는 정상 가동됩니다.:{}",e.getMessage());
        }
    }
}
