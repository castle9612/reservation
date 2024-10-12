package com.reservation.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AnnouncementDTO {
    private String title;
    private String content;
    private LocalDateTime creationDate;  // 작성일 추가
}
