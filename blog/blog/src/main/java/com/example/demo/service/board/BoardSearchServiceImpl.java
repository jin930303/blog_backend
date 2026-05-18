package com.example.demo.service.board;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.example.demo.document.BoardDocument;
import com.example.demo.entity.board.BoardEntity;
import com.example.demo.repository.board.BoardRepository;
import com.example.demo.repository.board.BoardSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardSearchServiceImpl implements BoardSearchService{

    private final BoardSearchRepository boardSearchRepository;
    private final BoardRepository boardRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // 1. Bulk Indexing (기존 DB 데이터 전체 ES 색인)
    @Transactional(readOnly = true)
    public void bulkIndex() {
        List<BoardEntity> allBoards = boardRepository.findAllWithHashtags();
        List<BoardDocument> documents = allBoards.stream()
                .map(BoardDocument::from)
                .collect(Collectors.toList());

        boardSearchRepository.saveAll(documents);
        log.info("[ES] bulk indexing 완료 - 총 {}건", documents.size());
    }

    // 2. 단건 색인 (게시글 작성 시)
    public void index(BoardEntity board) {
        boardSearchRepository.save(BoardDocument.from(board));
    }

    // 3. 단건 삭제 (게시글 삭제 시)
    public void delete(Long boardId) {
        boardSearchRepository.deleteById(boardId);
    }

    // 4. 키워드 검색 (커서 기반 페이징)
    public List<BoardDocument> searchByKeyword(String keyword, Long lastBoardId, int size) {
        // title, contentSummary 멀티 매치 검색
        Query matchQuery = MultiMatchQuery.of(m -> m
                .query(keyword)
                .fields("title", "contentSummary")
        )._toQuery();

        // 커서 조건: lastBoardId보다 작은 것만
        Query cursorQuery = buildCursorQuery(lastBoardId);

        Query finalQuery = lastBoardId != null
                ? BoolQuery.of(b -> b.must(matchQuery).filter(cursorQuery))._toQuery()
                : matchQuery;

        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withSort(s -> s.field(f -> f.field("boardId").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)))
                .withPageable(PageRequest.of(0, size))
                .build();

        SearchHits<BoardDocument> hits = elasticsearchOperations.search(query, BoardDocument.class);
        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    // 5. 태그 검색 (커서 기반 페이징)
    public List<BoardDocument> searchByTag(String tagName, Long lastBoardId, int size) {
        Query tagQuery = TermQuery.of(t -> t
                .field("hashtags")
                .value(tagName)
        )._toQuery();

        Query cursorQuery = buildCursorQuery(lastBoardId);

        Query finalQuery = lastBoardId != null
                ? BoolQuery.of(b -> b.must(tagQuery).filter(cursorQuery))._toQuery()
                : tagQuery;

        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withSort(s -> s.field(f -> f.field("boardId").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)))
                .withPageable(PageRequest.of(0, size))
                .build();

        SearchHits<BoardDocument> hits = elasticsearchOperations.search(query, BoardDocument.class);
        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    // 커서 조건 쿼리 빌더
    private Query buildCursorQuery(Long lastBoardId) {
        if (lastBoardId == null) return null;
        return RangeQuery.of(r -> r
                .number(n -> n
                        .field("boardId")
                        .lt((double) lastBoardId))
        )._toQuery();
    }
}
