package com.reservation.web.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {

    private String id;
    private String userId;

    @NotNull(message = "코스는 필수입니다.")
    private Long courseId;

    @NotNull(message = "예약 희망 일시는 필수입니다.")
    @Future(message = "예약 희망 일시는 현재보다 이후여야 합니다.")
    private LocalDateTime reservationDateTime;

    private String name;
    private String phoneNumber;
    private String status;

    public boolean isMemberReservation() {
        return userId != null && !userId.trim().isEmpty();
    }

    public boolean isGuestReservation() {
        return !isMemberReservation();
    }
}
