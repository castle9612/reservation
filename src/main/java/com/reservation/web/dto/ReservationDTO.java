package com.reservation.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationDTO {
    private String id;
    private String userId;
    private String courseId;
    private LocalDateTime reservationDateTime;
    private String name;
    private String phoneNumber;
}
