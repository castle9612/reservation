package com.reservation.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AnnouncementDTO {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private List<String> attachmentPaths;       // 저장된 파일 경로 리스트
    private List<String> originalAttachmentNames; // 원본 파일명 리스트
}