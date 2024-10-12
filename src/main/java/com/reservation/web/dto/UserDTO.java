package com.reservation.web.dto;

import com.reservation.web.entity.UserEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserDTO {
    private String userID;
    private String password;
    private String userName;  // name으로 변경
    private String userEmail; // email으로 변경
    private String phoneNumber;
    private String role;
    private String gender;
    private boolean maritalStatus;
    private String birthdate;
    private Boolean privacyConsent;
    private int packageCount;  // 패키지 남은 횟수 추가
    private String memo;        // 관리자 메모 추가

    public static UserDTO toUserDTO(UserEntity userEntity) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserID(userEntity.getUserId());
        userDTO.setPassword(userEntity.getPassword());
        userDTO.setUserName(userEntity.getName());
        userDTO.setUserEmail(userEntity.getEmail());
        userDTO.setPhoneNumber(userEntity.getPhoneNumber());
        userDTO.setRole(userEntity.getRole());
        userDTO.setGender(userEntity.getGender());
        userDTO.setMaritalStatus(userEntity.isMaritalStatus());
        userDTO.setBirthdate(userEntity.getBirthdate());
        userDTO.setPrivacyConsent(userEntity.getPrivacyConsent());
        userDTO.setPackageCount(userEntity.getPackageCount());
        userDTO.setMemo(userEntity.getMemo());
        return userDTO;
    }
}
