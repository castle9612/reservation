package com.reservation.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String username;
    private String password;
    private String phoneNumber;
    private String name;
    private String gender;
    private String maritalStatus;
    private String birthdate;
}
