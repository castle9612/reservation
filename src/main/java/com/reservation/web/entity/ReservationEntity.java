package com.reservation.web.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; // 추가
import lombok.AllArgsConstructor; // 추가
import org.hibernate.annotations.UuidGenerator; // Hibernate 6+에서 String ID 자동 생성 시 필요

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor // 필요
@AllArgsConstructor // 필요에 따라
@Table(name = "reservations")
public class ReservationEntity {

    @Id
    @GeneratedValue // 자동 생성 전략 사용 명시
    @UuidGenerator // Hibernate 6+에서 UUID를 생성하여 String ID로 사용
    private String id; // ID 타입을 String으로 유지

    private String userId; // 회원 예약일 경우

    // CourseEntity와의 관계를 매핑 (외래 키 제약 관련, ID 타입과 무관)
    @ManyToOne(fetch = FetchType.LAZY) // Reservation(N) -> Course(1) 관계
    @JoinColumn(name = "course_id", nullable = false) // reservations 테이블의 course_id 컬럼을 참조하며, NOT NULL
    private CourseEntity course; // CourseEntity 객체 필드 추가

    // private Long courseId; 필드는 @ManyToOne 관계로 대체되었으므로 제거합니다.

    @Column(nullable = false) // 예약 시간 필수
    private LocalDateTime reservationDateTime;

    @Column(nullable = false) // 상태 필수
    private String status; // 예: PENDING, CONFIRMED, CANCELLED

    private String name; // 비회원 예약일 경우

    private String phoneNumber; // 비회원 예약일 경우

    // 주의: ManyToOne 관계에서는 course 필드를 통해 course 정보를 접근해야 합니다.
    // 예: reservationEntity.getCourse().getId()
}