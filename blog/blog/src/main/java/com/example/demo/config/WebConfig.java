package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String ACTUAL_UPLOAD_ROOT = "C:" + File.separator + "upload";

//    @Override
//    public void addCorsMappings(CorsRegistry corsRegistry){
//        corsRegistry.addMapping("/api/v1/**")
//                .allowedOrigins("http://172.30.1.9:5500", "http://localhost:5500","http://172.30.1.26:5500","http://localhost:5173")
//                .allowedMethods("GET", "POST", "PUT", "DELETE","OPTIONS")
//                .allowedHeaders("*")
//                .allowCredentials(true)
//                .maxAge(3600);
//
//    }



    public void addResourceHandlers(ResourceHandlerRegistry registry){
        Path uploadPath = Paths.get(ACTUAL_UPLOAD_ROOT).toAbsolutePath().normalize();

        // 파일 시스템 URL 형식으로 변환 (예: file:/C:/upload/images/)
        // 끝에 '/'를 추가해야 디렉토리를 가리킬 수 있습니다.
        String fileLocation = "file:" + uploadPath.toString().replace('\\', '/') + "/";

        // 🚩 수정: 클라이언트의 요청 URL 경로를 /images/** 로 변경했습니다.
        // 클라이언트가 http://localhost:8000/images/0fd59b2c... 요청 시 이 핸들러가 처리합니다.
        registry.addResourceHandler("/images/**")
                .addResourceLocations(fileLocation)
                .setCachePeriod(0);

        System.out.println("✅ File Resource Handler configured: URL path [/images/**] -> " + fileLocation);
    }

}
