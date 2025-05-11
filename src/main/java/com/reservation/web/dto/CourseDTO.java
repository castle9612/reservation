package com.reservation.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CourseDTO {
    private long id;
    private String name;
    private Long staffId; // 담당자 ID

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime courseDateTime;

    private int durationMinutes;
    private double memberPrice;
    private double nonMemberPrice;

    public CourseDTO(long id, String name, Long staffId, LocalDateTime courseDateTime, int durationMinutes, double memberPrice, double nonMemberPrice) {
        this.id = id;
        this.name = name;
        this.staffId = staffId;
        this.courseDateTime = courseDateTime;
        this.durationMinutes = durationMinutes;
        this.memberPrice = memberPrice;
        this.nonMemberPrice = nonMemberPrice;
    }
}