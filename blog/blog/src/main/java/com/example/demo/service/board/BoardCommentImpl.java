package com.example.demo.service.board;

import com.example.demo.dto.board.CommentRequestDTO;
import com.example.demo.dto.board.CommentResponseDTO;
import com.example.demo.entity.board.BoardCommentEntity;
import com.example.demo.entity.board.BoardEntity;
import com.example.demo.entity.board.CommentLikeEntity;
import com.example.demo.entity.member.MemberEntity;
import com.example.demo.repository.board.BoardCommentRepository;
import com.example.demo.repository.board.BoardRepository;
import com.example.demo.repository.board.CommentLikeRepository;
import com.example.demo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BoardCommentImpl implements BoardCommentService{

    private final BoardCommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CommentLikeRepository commentLikeRepository;

    private MemberEntity getCurrentMember(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())){
            return null;
        }
        String username = authentication.getName();
        return memberRepository.findByUsername(username).orElse(null);
    }


    @Override
    public List<CommentResponseDTO> getCommentList(Long boardId) {

        MemberEntity currentMember = getCurrentMember();

        List<BoardCommentEntity> comments = commentRepository.findByBoardIdAndNotDeleted(boardId);

        return comments.stream().map(comment -> convertToDTO(comment,currentMember)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponseDTO createComment(Long boardId, CommentRequestDTO requestDTO){
        MemberEntity member = getCurrentMember();
        if(member ==null){
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        BoardEntity board = boardRepository.findById(boardId).orElseThrow(()-> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        BoardCommentEntity comment =BoardCommentEntity.builder()
                .content(requestDTO.getContent())
                .board(board)
                .member(member)
                .deleted(false)
                .likes(0)
                .build();
        BoardCommentEntity savedComment = commentRepository.save(comment);
        return convertToDTO(savedComment,member);
    }
    @Override
    @Transactional
    public CommentResponseDTO updateComment(Long boardId, Long commentId, CommentRequestDTO requestDTO){
        MemberEntity member = getCurrentMember();

        if(member == null){
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        BoardCommentEntity comment = commentRepository.findByCommentIdAndDeletedFalse(commentId).orElseThrow(()-> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if(comment.getMember().getMemberId() != member.getMemberId()){
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }
        comment.setContent(requestDTO.getContent());

        BoardCommentEntity updateComment = commentRepository.save(comment);
        return convertToDTO(updateComment,member);
    }

    @Override
    @Transactional
    public void deleteComment(Long boardId, Long commentId){
        MemberEntity member = getCurrentMember();

        if(member == null){
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        BoardCommentEntity comment = commentRepository.findByCommentIdAndDeletedFalse(commentId).orElseThrow(()-> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if(comment.getMember().getMemberId() != member.getMemberId()){
            throw  new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);

    }

    @Override
    @Transactional
    public Boolean toggleLike(Long boardId, Long commentId){
        MemberEntity member = getCurrentMember();

        if(member == null){
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        BoardCommentEntity comment = commentRepository.findByCommentIdAndDeletedFalse(commentId).orElseThrow(()-> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        Optional<CommentLikeEntity> existingLike = commentLikeRepository.findByCommentAndMember(comment,member);

        if(existingLike.isPresent()){
            commentLikeRepository.delete(existingLike.get());
            comment.setLikes(Math.max(0,comment.getLikes() -1));
            return false;
        } else{
            CommentLikeEntity like = CommentLikeEntity.builder()
                    .comment(comment)
                    .member(member)
                    .build();
            commentLikeRepository.save(like);
            comment.setLikes(comment.getLikes()+1);
            return true;
        }
    }


    private CommentResponseDTO convertToDTO(BoardCommentEntity comment, MemberEntity currentMember){
        boolean isAuthor =currentMember != null && comment.getMember().getMemberId() == currentMember.getMemberId();

        boolean isLiked = false;
        if(currentMember != null){
            isLiked = commentLikeRepository.countByCommentAndMember(comment,currentMember) > 0;
        }
        return CommentResponseDTO.builder()
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .inputDate(comment.getInputDate())
                .modifiedDate(comment.getModifiedDate())
                .deleted(comment.getDeleted())
                .likes(comment.getLikes())
                .memberId(comment.getMember().getMemberId())
                .nickname(comment.getMember().getNickname())
                .isAuthor(isAuthor)
                .isLikedByCurrentUser(isLiked)
                .build();
    }

}
