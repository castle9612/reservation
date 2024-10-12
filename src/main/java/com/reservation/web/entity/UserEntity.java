package com.reservation.web.entity;

import com.reservation.web.dto.UserDTO;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user")
public class UserEntity {

    @Id // Primary key
    private String userId;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(length = 100, nullable = false)
    private String name;  // userName에서 name으로 변경

    @Column(length = 100, nullable = false)
    private String email; // userEmail에서 email으로 변경

    @Column(length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "role", nullable = false)
    private String role = "user";  // 기본 role은 "user"

    @Column(length = 10)
    private String gender;

    @Column
    private boolean maritalStatus;

    @Column(length = 25)
    private String birthdate;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "privacy_consent", nullable = false)
    private Boolean privacyConsent;

    @Column(length = 1000)
    private String memo;  // 관리자 메모 필드 추가

    @Column(name = "package_count", nullable = false)
    private int packageCount;  // 패키지 남은 횟수 필드 추가

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    @Builder
    public static UserEntity toUserEntity(UserDTO userDTO) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(userDTO.getUserID());
        userEntity.setPassword(userDTO.getPassword());
        userEntity.setName(userDTO.getUserName());
        userEntity.setEmail(userDTO.getUserEmail());
        userEntity.setPhoneNumber(userDTO.getPhoneNumber());
        userEntity.setRole(userDTO.getRole() != null ? userDTO.getRole() : "user");
        userEntity.setGender(userDTO.getGender());
        userEntity.setMaritalStatus(userDTO.isMaritalStatus());
        userEntity.setBirthdate(userDTO.getBirthdate());
        userEntity.setPrivacyConsent(userDTO.getPrivacyConsent());
        userEntity.setPackageCount(userDTO.getPackageCount());
        userEntity.setMemo(userDTO.getMemo());
        return userEntity;
    }
}
