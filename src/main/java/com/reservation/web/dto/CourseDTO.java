package com.reservation.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CourseDTO {
    private String id;
    private String name;
    private String staff;
    private String courseDateTime;  // String 유지
    private double memberPrice;
    private double nonMemberPrice;
}
