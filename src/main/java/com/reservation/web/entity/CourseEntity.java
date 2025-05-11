package com.reservation.web.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private StaffEntity staff;

    @Column(nullable = false)
    private LocalDateTime courseDateTime;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Double memberPrice;

    @Column(nullable = false)
    private Double nonMemberPrice;

    public CourseEntity(String name, StaffEntity staff, LocalDateTime courseDateTime, Integer durationMinutes, Double memberPrice, Double nonMemberPrice) {
        this.name = name;
        this.staff = staff;
        this.courseDateTime = courseDateTime;
        this.durationMinutes = durationMinutes;
        this.memberPrice = memberPrice;
        this.nonMemberPrice = nonMemberPrice;
    }
}