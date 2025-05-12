package com.reservation.web.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    // ID 타입을 String으로 변경하여 Entity ID와 일관성을 맞춤
    private String id;

    private String userId; // 회원 예약 시 필요 (인증된 사용자 ID)
    private Long courseId; // 선택된 코스 ID (CourseEntity의 ID는 Long)
    private LocalDateTime reservationDateTime; // 예약 희망 일시
    private String name; // 비회원 예약 시 필요 (예약자 이름)
    private String phoneNumber; // 비회원 예약 시 필요 (예약자 연락처)

    // 예약 상태 필드 추가
    private String status; // 예: PENDING, CONFIRMED, CANCELLED
}