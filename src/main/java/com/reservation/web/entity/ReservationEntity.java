package com.reservation.web.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reservations")
public class ReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // Foreign key
    private Long courseId; // Foreign key
    private LocalDateTime reservationDateTime;
    private String reservationTime; // 예약 시간 필드 추가
    private String status; // 예: "PENDING", "CONFIRMED", "CANCELED"

    // 기타 필드 및 메소드
}
