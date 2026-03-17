package com.reservation.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ReviewDTO {
    private Long id;
    private String reviewerName;
    private String reviewerUserId;
    private Integer rating;
    private String content;
    private List<String> imagePaths = new ArrayList<>();
    private LocalDateTime createdAt;
    private boolean editable;
    private boolean deletable;
}
