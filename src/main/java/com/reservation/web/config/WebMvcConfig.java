package com.reservation.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String UPLOAD_DIR_NAME = "uploads"; // 폴더명
    private final Path UPLOAD_DIR_PATH = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR_NAME).toAbsolutePath();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 웹 요청 경로 /uploads/** 를 실제 파일 시스템 경로 file:./uploads/ 에 매핑
        registry.addResourceHandler("/" + UPLOAD_DIR_NAME + "/**")
                .addResourceLocations("file:" + UPLOAD_DIR_PATH.toString() + "/");
    }
}