package com.reservation.web.dto;

import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserMyPageDTO {

    // Getters and Setters
    private String name;
    private String phoneNumber;
    private String email;
    private String password;
    private int packageCount;
    private List<ReservationEntity> reservations;
    private String memo;  // 관리자 메모 추가

    public UserMyPageDTO(UserEntity user, List<ReservationEntity> reservations) {
        this.name = user.getName();
        this.phoneNumber = user.getPhoneNumber();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.packageCount = user.getPackageCount();
        this.reservations = reservations;
        this.memo = user.getMemo();  // 관리자 메모
    }

}
