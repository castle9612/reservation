package com.reservation.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CourseDTO {

    private Long id;
    private String name;
    private String staff;
    private String courseDateTime;  // 코스 날짜 및 시간 (String으로 변환)
    private double memberPrice;
    private double nonMemberPrice;
}
