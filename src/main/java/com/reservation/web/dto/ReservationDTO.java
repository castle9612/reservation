package com.reservation.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationDTO {
    private Long userId;
    private Long courseId;
    private LocalDateTime reservationDateTime;
}
