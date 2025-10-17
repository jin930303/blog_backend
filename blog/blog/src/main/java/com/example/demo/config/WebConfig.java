package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String ACTUAL_UPLOAD_ROOT = "C:" + File.separator + "upload";

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry){
        corsRegistry.addMapping("/api/v1/**")
                .allowedOrigins("http://172.30.1.9:5500", "http://localhost:5500")
                .allowedMethods("GET", "POST", "PUT", "DELETE","OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

    }



    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        Path uploadPath = Paths.get(ACTUAL_UPLOAD_ROOT).toAbsolutePath().normalize();
        String fileLocation = "file:" + uploadPath.toString().replace('\\', '/') + "/"; // file:/C:/upload/

        registry.addResourceHandler("/upload/**")
                .addResourceLocations(fileLocation)
                .setCachePeriod(0);
        System.out.println("✅ File Resource Handler configured: URL path [/upload/**] -> " + fileLocation);
    }

}
