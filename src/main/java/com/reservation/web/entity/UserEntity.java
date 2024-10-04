package com.reservation.web.entity;

import com.reservation.web.dto.UserDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user")
public class UserEntity {

    @Id // primary key
    private String userID;

    @Column(length = 100)
    private String password;

    @Column(length = 100)
    private String userName;

    @Column(length = 100, nullable = false)
    private String userEmail;

    @Column(length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "role")
    private String role = "user";

    @Column(length = 10)
    private String gender;

    @Column(length = 10)
    private boolean maritalStatus;

    @Column(length = 25)
    private String birthdate;

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

    @Column(name = "privacy_consent")
    private Boolean privacyConsent;

    @Builder
    public static UserEntity toUserEntity(UserDTO userDTO) {
        UserEntity userEntity = new UserEntity();

        userEntity.userID = userDTO.getUserID();
        userEntity.password = userDTO.getPassword();
        userEntity.userName = userDTO.getUserName();
        userEntity.userEmail = userDTO.getUserEmail();
        userEntity.phoneNumber = userDTO.getPhoneNumber();
        userEntity.role = userDTO.getRole() != null ? userDTO.getRole() : "user";

        // 추가 필드들
        userEntity.gender = userDTO.getGender();
        userEntity.maritalStatus = userDTO.isMaritalStatus();
        userEntity.birthdate = userDTO.getBirthdate();
        userEntity.privacyConsent = userDTO.getPrivacyConsent();

        return userEntity;
    }
}
