package com.example.demo.service.board;

import com.example.demo.dto.board.BoardDTO;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.entity.board.BoardEntity;
import com.example.demo.repository.board.BoardRepository;
import com.example.demo.service.file.FileService;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;


import com.vladsch.flexmark.util.ast.Node;
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
    public Long saveNewBoard(BoardDTO boardDTO) {

        final String finalFilePathToSave = null;
        final String originalFileName = null;
        final Long fileSize =0L;

        String htmlContent = markdownToHtml(boardDTO.getContent());

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
    public void updateBoard(BoardDTO boardDTO) {

        final String finalFilePathToSave = null;
        final String originalFileName = null;
        final Long fileSize =0L;

        BoardEntity board = boardRepository.findById(boardDTO.getBoardId())
                .orElseThrow(()-> new EntityNotFoundException("게시글을 찾을 수 없습니다. id :"+boardDTO.getBoardId()));

        String oldContent = board.getContent();

        String newHtmlContent = markdownToHtml(boardDTO.getContent());

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
                finalFilePathToSave
        );
    }

    @Override
    @Transactional
    public void deleteBoard(Long boardId) {
        BoardEntity entity = boardRepository.findById(boardId).orElseThrow(()->new EntityNotFoundException("게시글 ID를 찾을 수 없습니다."));
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
    public List<BoardResponse> findAllBoards() {
            List<BoardEntity> list = boardRepository.findAll();

        return list.stream()
                .map(BoardResponse::fromEntity )
                .collect(Collectors.toList());
    }

    @Override
    public BoardResponse findBoardById(Long id) {
        BoardEntity board = boardRepository.findById(id).orElseThrow(()->new EntityNotFoundException("게시판 아이디를 찾을 수 없습니다."+id));


        return BoardResponse.fromEntity(board,board.getContent());
    }

    @Override
    public String markdownHtml(String markdownText) {
        if(markdownText == null || markdownText.isEmpty()){
            return "";
        }
        return HTML_RENDERER.render(MARKDOWN_PARSER.parse(markdownText));
    }

}
