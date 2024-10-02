package com.reservation.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDTO {
    private String name;
    private double price;
    private Long staffId; // Foreign key
}
