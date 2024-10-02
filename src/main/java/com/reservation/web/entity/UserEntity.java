package com.reservation.web.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String username;

    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String name;

    private String gender;

    @Column(name = "marital_status")
    private String maritalStatus;

    private String birthdate;

    @Column(name = "privacy_consent")
    private Boolean privacyConsent;

}