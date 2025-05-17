package com.reservation.web.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AnnouncementDTO {
    private Long id; // 수정 시 필요
    private String title;
    private String content; // HTML 내용 포함
    private LocalDateTime createdAt;
    private List<String> attachmentPaths; // 기존 첨부 파일 경로 (상세/수정 폼에 표시용)

    // 폼에서 받을 파일들 (컨트롤러에서 @RequestParam으로 직접 받을 수도 있음)
    // private MultipartFile imageFile; // 대표 이미지용 (Summernote와 별개라면)
    // private List<MultipartFile> attachmentFiles; // 별도 첨부 파일용
}