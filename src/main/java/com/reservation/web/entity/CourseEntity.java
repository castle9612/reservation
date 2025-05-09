package com.reservation.web.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "courses")
public class CourseEntity {

    @Id
    private String id; // 변경됨

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String staff;

    @Column(nullable = false)
    private LocalDateTime courseDateTime;

    @Column(nullable = false)
    private double memberPrice;

    @Column(nullable = false)
    private double nonMemberPrice;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
