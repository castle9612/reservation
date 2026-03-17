package com.reservation.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMyPageDTO {
    private String userId;
    private String userName;
    private String phoneNumber;
    private String userEmail;
    private String password;
    private int packageCount;
    private String memo;
}
