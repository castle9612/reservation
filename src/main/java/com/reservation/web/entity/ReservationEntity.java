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

    private String userId; // Foreign key (회원의 경우)
    private Long courseId; // Foreign key

    @ManyToOne
    @JoinColumn(name = "courseId", insertable = false, updatable = false)
    private CourseEntity course;

    private LocalDateTime reservationDateTime;
    private String status; // 예: "PENDING", "CONFIRMED", "CANCELED"

    private String name; // 비회원 이름
    private String phoneNumber; // 비회원 전화번호

    // 기타 필드 및 메소드
}
