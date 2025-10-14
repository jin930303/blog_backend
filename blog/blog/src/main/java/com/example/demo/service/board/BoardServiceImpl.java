package com.example.demo.service.board;

import com.example.demo.dto.board.BoardDTO;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.entity.board.BoardEntity;
import com.example.demo.repository.board.BoardRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;

    public BoardServiceImpl(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    @Transactional
    public Long saveNewBoard(BoardDTO boardDTO) {
        BoardEntity entity = BoardEntity.builder()
                .title(boardDTO.getTitle())
                .content(boardDTO.getContent())
                .nickname(boardDTO.getNickname())
                .category(boardDTO.getCategory())
                .inputDate(LocalDateTime.now())
                .modifiedDate(LocalDateTime.now())
                .build();
        BoardEntity saveEntity = boardRepository.save(entity);
        return saveEntity.getBoardId();
    }

    @Override
    public List<BoardResponse> findAllBoards() {
            List<BoardEntity> list = boardRepository.findAll();

        return list.stream()
                .map(BoardResponse::fromEntity )
                .collect(Collectors.toList());
    }
}
