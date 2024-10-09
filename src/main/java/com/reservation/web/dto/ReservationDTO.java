package com.reservation.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationDTO {
    private Long userId; // 회원일 경우
    private Long courseId;
    private LocalDateTime reservationDateTime;
    private String name; // 비회원일 경우
    private String phoneNumber; // 비회원일 경우
}
