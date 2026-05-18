package com.example.demo.repository.board;

import com.example.demo.document.BoardDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BoardSearchRepository extends ElasticsearchRepository<BoardDocument,Long> {
}
