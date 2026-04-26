package com.reservation.web.entity;

import com.reservation.web.dto.UserDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
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

    @Id
    private String userId;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 20, nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "role", nullable = false)
    private String role = "USER";

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
    private String memo;

    @Column(name = "package_count", nullable = false)
    private int packageCount;

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
        userEntity.setUserId(userDTO.getUserId());
        userEntity.setPassword(userDTO.getPassword());
        userEntity.setName(userDTO.getUserName());
        userEntity.setEmail(userDTO.getUserEmail());
        userEntity.setPhoneNumber(userDTO.getPhoneNumber());
        userEntity.setRole(normalizeRole(userDTO.getRole()));
        userEntity.setGender(userDTO.getGender());
        userEntity.setMaritalStatus(userDTO.isMaritalStatus());
        userEntity.setBirthdate(userDTO.getBirthdate());
        userEntity.setPrivacyConsent(Boolean.TRUE.equals(userDTO.getPrivacyConsent()));
        userEntity.setPackageCount(userDTO.getPackageCount());
        userEntity.setMemo(userDTO.getMemo());
        return userEntity;
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }

        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        return normalized;
    }
}
