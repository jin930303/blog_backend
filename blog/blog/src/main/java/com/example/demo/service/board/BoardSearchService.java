package com.example.demo.service.board;

import com.example.demo.document.BoardDocument;
import com.example.demo.entity.board.BoardEntity;

import java.util.List;

public interface BoardSearchService {

    void index(BoardEntity boardForIndex);

    void delete(Long boardId);

    List<BoardDocument> searchByKeyword(String trim, Long lastBardId, int size);

    void bulkIndex();

    List<BoardDocument> searchByTag(String trim, Long lastBoardId, int size);
}
