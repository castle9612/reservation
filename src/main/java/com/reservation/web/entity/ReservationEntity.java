package com.reservation.web.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "reservations")
public class ReservationEntity {
    @Id
    private String id;

    private String userId;

    private String courseId;

    private LocalDateTime reservationDateTime;

    private String status;

    private String name;

    private String phoneNumber;
}
