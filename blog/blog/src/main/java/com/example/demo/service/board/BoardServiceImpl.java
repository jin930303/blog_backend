package com.example.demo.service.board;

import com.example.demo.dto.board.BoardDTO;
import com.example.demo.dto.board.BoardListResponse;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.entity.board.BoardEntity;
import com.example.demo.entity.member.MemberEntity;
import com.example.demo.repository.board.BoardRepository;
import com.example.demo.repository.member.MemberRepository;
import com.example.demo.service.file.FileService;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;


import com.vladsch.flexmark.util.data.MutableDataSet;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final MutableDataSet OPTIONS = new MutableDataSet();
    private static final Parser MARKDOWN_PARSER = Parser.builder(OPTIONS).build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder(OPTIONS).build();

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

    @Override
    @Transactional
    public Long saveNewBoard(BoardDTO boardDTO, Long currentMemberId) {

        final String finalFilePathToSave = null;
        final String originalFileName = null;
        final Long fileSize =0L;
        MemberEntity author = memberRepository.findById(currentMemberId)
                .orElseThrow(()->new EntityNotFoundException("작성자 ID 를 찾을 수 없습니다. 게시글을 작성 할 수 없습니다."));
        String htmlContent = markdownToHtml(boardDTO.getContent());

        String textForSummary = htmlContent;
        final int SUMMARY_LENGTH = 200;
        String contentSummary = textForSummary;

        if(textForSummary != null && textForSummary.length() > SUMMARY_LENGTH){
            contentSummary = textForSummary.substring(0,SUMMARY_LENGTH);
        }

        BoardEntity entity = BoardEntity.builder()
                .title(boardDTO.getTitle())
                .content(htmlContent)
                .nickname(boardDTO.getNickname())
                .category(boardDTO.getCategory())
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
    public void updateBoard(BoardDTO boardDTO,Long currentMemberId) throws AccessDeniedException {

        final String finalFilePathToSave = null;
        final String originalFileName = null;
        final Long fileSize =0L;

        BoardEntity board = boardRepository.findById(boardDTO.getBoardId())
                .orElseThrow(()-> new EntityNotFoundException("게시글을 찾을 수 없습니다. id :"+boardDTO.getBoardId()));


        Long authorId = (board.getMember() != null) ? board.getMember().getMemberId() : null;

        if(authorId == null || currentMemberId == null || !currentMemberId.equals(authorId)){
            log.warn("수정 : 게시글 작성자와 아이디가 일치 하지 않습니다. 요청 {}, 작성자 {}",currentMemberId,authorId);
            throw new AccessDeniedException("수정 권한이 없습니다. 게시글 작성자만 수정할 수 있습니다.");
        }


        String oldContent = board.getContent();

        String newHtmlContent = markdownToHtml(boardDTO.getContent());

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
                boardDTO.getTitle(),
                boardDTO.getNickname(),
                newHtmlContent,
                boardDTO.getCategory(),
                originalFileName,
                fileSize,
                finalFilePathToSave,
                contentSummary


        );
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
    public void increaseView(Long boardId) {
        boardRepository.increaseView(boardId);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardListResponse getBoardsWithCursor(int size, Long cursorId, LocalDateTime cursorDate,Long currentMemberId) {

        // 1. 요청된 크기보다 하나 더 가져와서 다음 페이지 존재 여부 확인 (size + 1)
        int pageSize = size + 1;

        if(cursorDate == null){
            cursorDate = LocalDateTime.now();
            log.info("첫 페이지 조회: cursorDate를 현재 시간({})으로 설정했습니다. CursorId: {}", cursorDate, cursorId);
        }
        // ⭐ 1. Repository는 BoardEntity 목록을 반환해야 합니다. (Service에서 DTO 변환)
        // boards는 size + 1개의 Entity를 포함합니다.
        List<BoardEntity> boards = boardRepository.findNextBoardsWithMember(cursorId, cursorDate,pageSize);

        // 2. 다음 페이지 존재 여부 판단
        boolean hasNext = boards.size() > size;

        // 3. 실제 표시할 게시글 목록 (요청된 크기까지만)
        // boards는 size + 1개이므로, subList(0, size)를 통해 size개만 contentEntities에 담깁니다.
        List<BoardEntity> contentEntities = hasNext ? boards.subList(0, size) : boards;

        // 4. Entity를 DTO로 변환
        List<BoardResponse> contentDtos = contentEntities.stream()
                .map(entity -> {
                    Long authorId = (entity.getMember() != null) ? entity.getMember().getMemberId() : null;
                            boolean isAuthor = (currentMemberId != null && authorId != null && currentMemberId.equals(authorId));
                            return BoardResponse.fromEntity(entity, entity.getContentSummary(), isAuthor);
                        })
                .collect(Collectors.toList());
        // 5. 다음 커서 값 설정
        Long nextCursorId = null;
        LocalDateTime nextCursorDate = null;

        if (hasNext) {
            // ⭐ 2. 커서 추출 로직 수정:
            // 커서는 Repository에서 가져온 전체 목록(boards) 중
            // 현재 페이지에 표시되지 않은 다음 항목 (인덱스 size)에서 추출해야 합니다.
            BoardEntity cursorEntity = boards.get(size);

            nextCursorId = cursorEntity.getBoardId();
            nextCursorDate = cursorEntity.getInputDate();
        }

        // BoardListResponse DTO로 반환
        return new BoardListResponse(
                contentDtos,
                hasNext,
                nextCursorId,
                nextCursorDate
        );

    }

    @Override
    public List<BoardResponse> findAllBoards(Long currentMemberId) {
            List<BoardEntity> list = boardRepository.findAllWithMember();

        return list.stream()
                .map(entity -> {
                    Long authorId = (entity.getMember() != null) ? entity.getMember().getMemberId() : null;
                    boolean isAuthor = (currentMemberId != null && authorId != null && currentMemberId.equals(authorId));
                    return BoardResponse.fromEntity(entity,isAuthor);
                } )
                .collect(Collectors.toList());
    }

    @Override
    public BoardResponse findBoardById(Long id, Long currentMemberId) {
        BoardEntity board = boardRepository.findById(id).orElseThrow(()->new EntityNotFoundException("게시판 아이디를 찾을 수 없습니다."+id));

        Long authorId = (board.getMember() != null) ? board.getMember().getMemberId() : null;

        boolean isAuthor = (currentMemberId != null && authorId != null && currentMemberId.equals(authorId));

        return BoardResponse.fromEntity(board,board.getContent(),isAuthor);
    }

    @Override
    public String markdownHtml(String markdownText) {
        if(markdownText == null || markdownText.isEmpty()){
            return "";
        }
        return HTML_RENDERER.render(MARKDOWN_PARSER.parse(markdownText));
    }

}
