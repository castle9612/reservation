package com.reservation.web.dto;

import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// UserMyPageDTO.java
@Setter
@Getter
public class UserMyPageDTO {
    private String userId; // ⭐️ 추가
    private String userName; // name -> userName (UserDTO와 일관성)
    private String phoneNumber;
    private String userEmail; // email -> userEmail (UserDTO와 일관성)
    private String password; // 새 비밀번호 입력용
    private int packageCount;
    private List<ReservationEntity> reservations; // 필요시 DTO로 변환
    private String memo;

    public UserMyPageDTO() {} // 기본 생성자

    // 생성자 (UserEntity로 초기화)
    public UserMyPageDTO(UserEntity user, List<ReservationEntity> reservations) {
        this.userId = user.getUserId(); // ⭐️ 추가
        this.userName = user.getName();
        this.phoneNumber = user.getPhoneNumber();
        this.userEmail = user.getEmail();
        // password는 보통 엔티티에서 직접 가져와 DTO에 설정하지 않음 (수정 시에만 사용)
        this.packageCount = user.getPackageCount();
        this.reservations = reservations; // 필요시 ReservationDTO 리스트로 변환
        this.memo = user.getMemo();
    }
}