package com.example.demo.service.board;

import com.example.demo.entity.board.BoardEntity;
import com.example.demo.entity.board.BoardLikeEntity;
import com.example.demo.entity.member.MemberEntity;
import com.example.demo.repository.board.BoardLikeRepository;
import com.example.demo.repository.board.BoardRepository;
import com.example.demo.repository.member.MemberRepository;
import com.example.demo.service.notification.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardLikeServiceImpl implements BoardLikeService{

    private final BoardLikeRepository boardLikeRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Boolean toggleLike(Long boardId, Long memberId) {

        Optional<BoardLikeEntity> existingLike = boardLikeRepository.findByMemberIdAndBoardId(memberId,boardId);

        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(()-> new EntityNotFoundException("게시글 ID를 찾을 수 없습니다."+boardId));

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(()->new EntityNotFoundException("회원 ID를 찾을 수 없습니다."+memberId));

        if(existingLike.isPresent()){
            log.info("좋아요 취소 : boardId = {}, memberId = {}",boardId,memberId);

            boardLikeRepository.deleteByMemberIdAndBoardId(memberId, boardId);

            board.setLikes(board.getLikes() - 1);

            return false;
        }
        else{
            log.info("좋아요 생성 : boardId={},memberId={}",boardId,memberId);

            BoardLikeEntity newLike = BoardLikeEntity.builder()
                    .board(board)
                    .member(member)
                    .build();
            boardLikeRepository.save(newLike);

            board.setLikes(board.getLikes() + 1);

           MemberEntity writer = board.getMember();
           if(writer.getMemberId() != member.getMemberId()){
               notificationService.send(
                       writer,member.getNickname()+"님이 회원님의 게시글을 좋아합니다.",
                       "/board/"+boardId
               );
           }

            return true;
        }

    }

    @Override
    public boolean isBoardLikedByUser(Long boardId, Long currentMemberId) {

        Long count = boardLikeRepository.isBoardLikedByUser(boardId,currentMemberId);

        return count != null && count >0 ;
    }
}
