package com.example.demo.service.file;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Log4j2
public class FileServiceImpl implements FileService{

    private final String BASE_URL = "http://localhost:8000/images/";

    @Value("${upload.file.path}")
    private String uploadDir;


    private Map<String, Object> saveFileAndGetMetadata(MultipartFile file, String targetDir) throws IOException {
        if(file ==null || file.isEmpty()){
            return new HashMap<>();
        }

        String originalFileName = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String saveFileName = uuid + "_" + originalFileName;

        File uploadDirectory = new File(targetDir);
        if(!uploadDirectory.exists()){
            uploadDirectory.mkdirs();
        }

        File saveFile = new File(targetDir, saveFileName);
        file.transferTo(saveFile);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("originalFileName",originalFileName);
        metadata.put("saveFileName",saveFileName);
        metadata.put("filePath",saveFile.getAbsolutePath());
        metadata.put("fileSize",file.getSize());
        return metadata;
    }

    @Override
    public Map<String, Object> saveBoardAttachment(MultipartFile mulfile,String uploadDir) throws IOException {
        return saveFileAndGetMetadata(mulfile,uploadDir);
    }

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        Map<String,Object> metadata = saveFileAndGetMetadata(file,this.uploadDir);
        if(metadata.isEmpty()){
            return "";
        }
        String saveFileName = (String) metadata.get("saveFileName");
        return BASE_URL + saveFileName;
    }

    @Override
    public void deleteFile(String physicalFilePath) {
        if(physicalFilePath == null || physicalFilePath.isEmpty()){
            log.warn("삭제할 파일 경로가 유효하지 않습니다 : {}",physicalFilePath);
            return;
        }
        try{
            Path filePath = new File(physicalFilePath).toPath();

            if(Files.exists(filePath)){
                Boolean deleted = Files.deleteIfExists(filePath);
                if(deleted){
                    log.info("파일 삭제 성공 : {}",physicalFilePath);
                }
                else{
                    log.warn("파일 삭제 실패 {}",physicalFilePath);
            }
            }else {
                log.warn("파일 삭제 시도 : 파일이 존재하지 않거나 이미 삭제되었습니다. 경로 : {}",physicalFilePath);
            }
        } catch (IOException e){
            log.error("파일 삭제 중 I/O 오류 발생. 경로 : {}",physicalFilePath, e);
        } catch (SecurityException e){
            log.error("파일 삭제 중 보안 오류 발생. 경로 : {}",physicalFilePath, e);
        }
    }
}
