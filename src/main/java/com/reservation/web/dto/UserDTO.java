package com.reservation.web.dto;

import com.reservation.web.entity.UserEntity;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserDTO {
    private String userID;
    private String password;
    private String userName;
    private String userEmail;
    private String phoneNumber;
    private String role;
    private String gender;
    private boolean maritalStatus;
    private String birthdate;
    private Boolean privacyConsent;

    public static UserDTO toUserDTO(UserEntity userEntity) {
        UserDTO userDTO = new UserDTO();

        userDTO.setUserID(userEntity.getUserID());
        userDTO.setPassword(userEntity.getPassword());
        userDTO.setUserName(userEntity.getUserName());
        userDTO.setUserEmail(userEntity.getUserEmail());
        userDTO.setPhoneNumber(userEntity.getPhoneNumber());
        userDTO.setRole(userEntity.getRole());

        // 추가 필드들
        userDTO.setGender(userEntity.getGender());
        userDTO.setMaritalStatus(userEntity.isMaritalStatus());
        userDTO.setBirthdate(userEntity.getBirthdate());
        userDTO.setPrivacyConsent(userEntity.getPrivacyConsent());

        return userDTO;
    }
}
