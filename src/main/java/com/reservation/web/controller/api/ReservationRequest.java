package com.reservation.web.controller.api;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationRequest {

    @NotNull(message = "코스는 필수입니다.")
    private Long courseId;

    @NotNull(message = "예약 희망 일시는 필수입니다.")
    @Future(message = "예약 희망 일시는 현재보다 이후여야 합니다.")
    private LocalDateTime reservationDateTime;

    private String name;
    private String phoneNumber;
}