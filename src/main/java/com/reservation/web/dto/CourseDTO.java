package com.reservation.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private long id;
    private String name;
    private Long staffId;
    private int durationMinutes;
    private double memberPrice;
    private double nonMemberPrice;
    private StaffDTO staff; // 추가: 담당 직원 정보 (이름 등)
}
