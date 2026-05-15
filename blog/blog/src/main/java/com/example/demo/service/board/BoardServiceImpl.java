package com.example.demo.service.board;

import com.example.demo.dto.board.BoardRequestDTO;
import com.example.demo.dto.board.BoardListResponse;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.entity.board.BoardEntity;
import com.example.demo.entity.board.BoardHashtagEntity;
import com.example.demo.entity.board.HashtagEntity;
import com.example.demo.entity.member.MemberEntity;
import com.example.demo.repository.board.BoardHashtagRepository;
import com.example.demo.repository.board.BoardRepository;
import com.example.demo.repository.board.BoardSummary;
import com.example.demo.repository.board.HashtagRepository;
import com.example.demo.repository.member.MemberRepository;
import com.example.demo.service.file.FileService;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;


import com.vladsch.flexmark.util.data.MutableDataSet;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;

    //hashtag
    private final BoardHashtagRepository boardHashtagRepository;
    private final HashtagRepository hashtagRepository;

    private static final MutableDataSet OPTIONS = new MutableDataSet();
    private static final Parser MARKDOWN_PARSER = Parser.builder(OPTIONS).build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder(OPTIONS).build();

    private final RedisTemplate<String, String> redisTemplate;
    private final BoardRedisService redisService;

    @Value("${upload.file.path}")
    private String uploadDir;

    private String markdownToHtml(String markdown){
        if(markdown == null || markdown.isEmpty()){
            return "";
        }

        return HTML_RENDERER.render(MARKDOWN_PARSER.parse(markdown));
    }

    private Set<String> extractImageUrls(String htmlContent){
        Set<String> urls = new HashSet<>();

        Pattern pattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
        Matcher matcher = pattern.matcher(htmlContent);

        while(matcher.find()){
            urls.add(matcher.group(1));
        }
        return urls;
    }

    private String getPhysicalFilePath(String imageUrl){
        try{
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/")+1);

            return uploadDir + (uploadDir.endsWith(File.separator) ? "" : File.separator) + fileName;
        }catch(Exception e){
            log.warn("이미지 URL에서 파일 이름 추출 실패 : {}",imageUrl);
            return null;
        }
    }

    private void saveHashtags(BoardEntity board, List<String> tagNames){
        if(tagNames ==null || tagNames.isEmpty()){
            return;
        }
        for(String tagName : tagNames){
            // 공백 제거 및 # 제거
            String name = tagName.trim().replace("#","");
            if(name.isEmpty()) continue;
            // 태그가 존재하면 가져오고 없으면 새로 저장
            HashtagEntity hashtag = hashtagRepository.findByName(name)
                    .orElseGet(() -> hashtagRepository.save(new HashtagEntity(name)));
            // 게시글- 태그 연결 정보 저장
            boardHashtagRepository.save(new BoardHashtagEntity(board,hashtag));
        }
    }

    @Override
    @Transactional
    public Long saveNewBoard(BoardRequestDTO boardRequestDTO, Long currentMemberId) {

        final String finalFilePathToSave = null;
        final String originalFileName = null;
        final Long fileSize =0L;
        MemberEntity author = memberRepository.findById(currentMemberId)
                .orElseThrow(()->new EntityNotFoundException("작성자 ID 를 찾을 수 없습니다. 게시글을 작성 할 수 없습니다."));
        String htmlContent = markdownToHtml(boardRequestDTO.getContent());

        String textForSummary = htmlContent;
        final int SUMMARY_LENGTH = 200;
        String contentSummary = textForSummary;

        if(textForSummary != null && textForSummary.length() > SUMMARY_LENGTH){
            contentSummary = textForSummary.substring(0,SUMMARY_LENGTH);
        }

        BoardEntity entity = BoardEntity.builder()
                .title(boardRequestDTO.getTitle())
                .content(htmlContent)
                .nickname(boardRequestDTO.getNickname())
                .category(boardRequestDTO.getCategory())
                .inputDate(LocalDateTime.now())
                .modifiedDate(LocalDateTime.now())
                .fileOriginalName(originalFileName)
                .fileSize(fileSize)
                .filePath(finalFilePathToSave)
                .views(0)
                .likes(0)
                .contentSummary(contentSummary)
                .member(author)
                .build();
        BoardEntity saveEntity = boardRepository.save(entity);
        // 해시태그 저장 호출
        if(boardRequestDTO.getTags() != null){
            saveHashtags(saveEntity, boardRequestDTO.getTags());
        }

        return saveEntity.getBoardId();
    }

    @Override
    public String uploadFile(MultipartFile file){
        try{
            return fileService.uploadFile(file);
        }
        catch(IOException e){
            throw new RuntimeException("이미지 파일 업로드 중 오류가 발생했습니다.",e);
        }
    }

    @Override
    @Transactional
    public void updateBoard(BoardRequestDTO boardRequestDTO, Long currentMemberId) throws AccessDeniedException {

        final String finalFilePathToSave = null;
        final String originalFileName = null;
        final Long fileSize =0L;

        BoardEntity board = boardRepository.findById(boardRequestDTO.getBoardId())
                .orElseThrow(()-> new EntityNotFoundException("게시글을 찾을 수 없습니다. id :"+ boardRequestDTO.getBoardId()));


        Long authorId = (board.getMember() != null) ? board.getMember().getMemberId() : null;

        if(authorId == null || currentMemberId == null || !currentMemberId.equals(authorId)){
            log.warn("수정 : 게시글 작성자와 아이디가 일치 하지 않습니다. 요청 {}, 작성자 {}",currentMemberId,authorId);
            throw new AccessDeniedException("수정 권한이 없습니다. 게시글 작성자만 수정할 수 있습니다.");
        }


        String oldContent = board.getContent();

        String newHtmlContent = markdownToHtml(boardRequestDTO.getContent());

        String textForSummary = newHtmlContent;
        final int SUMMARY_LENGTH = 200;
        String contentSummary = textForSummary;

        if(textForSummary !=null && textForSummary.length() > SUMMARY_LENGTH){
            contentSummary = textForSummary.substring(0,SUMMARY_LENGTH);
        }


        Set<String> oldImageUrls = extractImageUrls(oldContent);
        Set<String> newImageUrls = extractImageUrls(newHtmlContent);

        Set<String> deletedImageUrls = new HashSet<>(oldImageUrls);
        deletedImageUrls.removeAll(newImageUrls);

        log.info("총 {}개의 파일이 마크다운에서 삭제되었습니다. 삭제 대상 파일 : {}",deletedImageUrls.size(),deletedImageUrls);

        for(String deleteUrl : deletedImageUrls){
            String physicalFilePath = getPhysicalFilePath(deleteUrl);
            if(physicalFilePath != null){
                try{
                    fileService.deleteFile(physicalFilePath);
                }catch (Exception e){
                    log.error("로컬 파일 삭제 실패 (경로 : {}): {}",physicalFilePath, e.getMessage());
                }
            }
        }

        board.update(
                boardRequestDTO.getTitle(),
                boardRequestDTO.getNickname(),
                newHtmlContent,
                boardRequestDTO.getCategory(),
                originalFileName,
                fileSize,
                finalFilePathToSave,
                contentSummary
        );
        //해시태그 업데이트 로직
        // 1. 기존 연결 모두 삭제
        boardHashtagRepository.deleteByBoardId(board.getBoardId());

        // 2. 새 태그 목록 저장
        if(boardRequestDTO.getTags() != null){
            saveHashtags(board, boardRequestDTO.getTags());
        }
    }

    @Override
    @Transactional
    public void deleteBoard(Long boardId, Long currentMemberId) throws AccessDeniedException {
        BoardEntity entity = boardRepository.findById(boardId).orElseThrow(()->new EntityNotFoundException("게시글 ID를 찾을 수 없습니다."));

        Long authorId = (entity.getMember() != null) ? entity.getMember().getMemberId() : null;

        if(authorId == null || currentMemberId == null || !currentMemberId.equals(authorId)){
            log.warn("삭제 : 게시글 작성자와 아이디가 일치 하지 않습니다. 요청 {}, 작성자 {}",currentMemberId,authorId);
            throw new AccessDeniedException("본인 게시글만 삭제 가능합니다.");
        }

        if(entity.getContent() !=null && !entity.getContent().isEmpty()){

            Set<String> imgUrls =  extractImageUrls(entity.getContent());
            log.info("게시글 ID {} 삭제 전, 본문에서 추출된 이미지 파일 URL 개수: {}", boardId, imgUrls.size());

            for(String imageUrl : imgUrls){
                String physicalFilePath = getPhysicalFilePath(imageUrl);
                if(physicalFilePath != null){
                    try{
                        fileService.deleteFile(physicalFilePath);
                        log.info("로컬 파일 삭제 성공 : {}",physicalFilePath);
                    }catch(Exception e){
                        log.error("로컬 파일 삭제 실패 경로 : {}, {}",physicalFilePath,e.getMessage());
                    }
                }
            }
        }
        if(entity.getFilePath() !=null && !entity.getFilePath().isEmpty()){
            try {
                String physicalFilePath = getPhysicalFilePath(entity.getFilePath());
                if (physicalFilePath != null) {
                    fileService.deleteFile(physicalFilePath);
                    log.info("로컬 첨부 파일 삭제 성공: {}", physicalFilePath);
                }
            } catch (Exception e) {
                log.error("로컬 첨부 파일 삭제 실패 (경로 : {}): {}", entity.getFilePath(), e.getMessage());
            }
        }
        boardRepository.deleteById(boardId);
        log.info("게시글 ID 삭제 완료 {}",boardId);
    }

    @Override
    @Transactional
    public void increaseView(Long boardId, String clientIdentifier) {
        redisService.increaseViewCount(boardId,clientIdentifier);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardListResponse getBoardsWithCursor(int size, Long cursorId, LocalDateTime cursorDate, Long currentMemberId) {

        if (cursorDate == null) {
            cursorDate = LocalDateTime.now();
        }

        // 1. 오라클 11g 호환 네이티브 쿼리 호출 (size + 1개 요청)
        int limitSize = size + 1;
        List<BoardSummary> summaryList = boardRepository.findBoardListNative(cursorId, cursorDate, limitSize);

        // 2. 다음 페이지 존재 여부 확인
        boolean hasNext = false;
        if (summaryList.size() > size) {
            hasNext = true;
            summaryList.remove(size); // 마지막 확인용 1개 제거
        }

        // 3. BoardSummary -> BoardResponse 변환
        // (DTO 생성자를 호출하면 이미지 경로 변환 로직이 자동 실행됨)
        List<BoardResponse> boardResponses = summaryList.stream()
                .map(s -> new BoardResponse(
                        s.getBoardId(),
                        s.getTitle(),
                        s.getContentSummary(),
                        s.getNickname(),
                        s.getFilePath(),
                        s.getFileOriginalName(),
                        s.getFileSize(),
                        s.getInputDate(),
                        s.getModifiedDate(),
                        null, // content (리스트에선 안 씀)
                        s.getViews(),
                        s.getLikes(),
                        s.getCategory(),
                        null, // tags
                        false // isAuthor
                ))
                .toList();

        // 4. 다음 커서 계산
        Long nextCursorId = null;
        LocalDateTime nextCursorDate = null;

        if (!boardResponses.isEmpty()) {
            BoardResponse lastBoard = boardResponses.get(boardResponses.size() - 1);
            nextCursorId = lastBoard.boardId();
            nextCursorDate = lastBoard.inputDate();
        }

        return new BoardListResponse(
                boardResponses,
                hasNext,
                nextCursorId,
                nextCursorDate
        );
    }

    @Override
    public BoardEntity findBoardByIdExceptUser(Long boardId) {
        BoardEntity entity = boardRepository.findById(boardId)
                .orElseThrow(()-> new EntityNotFoundException("게시판 아이디를 찾을 수 없습니다."+boardId));
        return entity;
    }

    @Override
    public List<BoardResponse> searchBoards(String keyword, String tagName, Long currentMemberId, Long lastBardId) {

        String searchKeyword = null;
        if(keyword != null && !keyword.trim().isEmpty()){
            searchKeyword = "%" + keyword.trim()+"%";
        }

        String searchTagName = null;
        if(tagName != null && !tagName.trim().isEmpty()){
            searchTagName = tagName.trim();
        }

        List<Object[]> rows = boardRepository.searchBoards(searchKeyword,searchTagName,lastBardId);

        return rows.stream()
                .map(row -> {
                    Long boardId = ((Number) row[0]).longValue();
                    String title = (String) row[1];
                    String contentSummary = (String) row[2];
                    String nickname = (String) row[3];
                    String filePath = (String) row[4];
                    String fileOriginalName = (String) row[5];
                    Long fileSize = row[6] != null ? ((Number) row[6]).longValue() : null;
                    LocalDateTime inputDate = row[7] != null
                            ? ((java.sql.Timestamp) row[7]).toLocalDateTime() : LocalDateTime.now();
                    LocalDateTime modifiedDate = row[8] != null
                            ? ((java.sql.Timestamp) row[8]).toLocalDateTime() : LocalDateTime.now();
                    int likes = row[9] != null ? ((Number) row[9]).intValue() : 0;
                    int views = row[10] != null ? ((Number) row[10]).intValue() : 0;
                    String category = (String) row[11];
                    Long authorId = row[12] != null ? ((Number) row[12]).longValue() : null;
                    boolean isAuthor = currentMemberId != null && currentMemberId.equals(authorId);

                    return new BoardResponse(
                            boardId, title, contentSummary, nickname,
                            filePath, fileOriginalName, fileSize,
                            inputDate, modifiedDate,
                            null,  // content 제외
                            likes, views, category,
                            Collections.emptyList(),
                            isAuthor
                    );
                })
                .toList();
    }

    @Override
    public List<BoardResponse> findAllBoards(Long currentMemberId) {
            List<BoardEntity> list = boardRepository.findAllWithMember();

        return list.stream()
                .map(entity -> {
                    Long authorId = (entity.getMember() != null) ? entity.getMember().getMemberId() : null;
                    boolean isAuthor = (currentMemberId != null && authorId != null && currentMemberId.equals(authorId));
                    return BoardResponse.fromEntity(entity,Collections.emptyList(),entity.getContentSummary(),isAuthor);
                } )
                .collect(Collectors.toList());
    }

    @Override
    public BoardResponse findBoardById(Long id, Long currentMemberId) {
        BoardEntity board = boardRepository.findById(id).orElseThrow(()->new EntityNotFoundException("게시판 아이디를 찾을 수 없습니다."+id));

        Long authorId = (board.getMember() != null) ? board.getMember().getMemberId() : null;
        boolean isAuthor = (currentMemberId != null && authorId != null && currentMemberId.equals(authorId));
        List<BoardHashtagEntity> boardHashtags = boardHashtagRepository.findAllByBoardId(id);
        List<String> tags = boardHashtags.stream()
                .map(bh -> bh.getHashtag().getName())
                .toList();
        return BoardResponse.fromEntity(board,tags,board.getContent(),isAuthor);
    }

    @Override
    public String markdownHtml(String markdownText) {
        if(markdownText == null || markdownText.isEmpty()){
            return "";
        }
        return HTML_RENDERER.render(MARKDOWN_PARSER.parse(markdownText));
    }

}
