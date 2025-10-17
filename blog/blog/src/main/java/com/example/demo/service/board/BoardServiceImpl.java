package com.example.demo.service.board;

import com.example.demo.dto.board.BoardDTO;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.entity.board.BoardEntity;
import com.example.demo.repository.board.BoardRepository;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;


import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;

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

    @Override
    @Transactional
    public Long saveNewBoard(BoardDTO boardDTO) {

        MultipartFile mulFile = boardDTO.getImg();
        String finalFilePathToSave = null;
        String originalFileName = null;
        Long fileSize =0L;

        if(mulFile !=null && !mulFile.isEmpty()) {
            try {
                String uuid = UUID.randomUUID().toString();
                originalFileName = mulFile.getOriginalFilename();
                String saveFileName = uuid + "_" + originalFileName;

                File uploadDirectory = new File(uploadDir);
                if (!uploadDirectory.exists()) {
                    uploadDirectory.mkdirs();
                }
                File saveFile = new File(uploadDir, saveFileName);
                mulFile.transferTo(saveFile);

                finalFilePathToSave = saveFile.getAbsolutePath();
                fileSize= mulFile.getSize();
            }
            catch(IOException e){
                throw new RuntimeException("파일 업로드 실패",e);
            }

        }
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
