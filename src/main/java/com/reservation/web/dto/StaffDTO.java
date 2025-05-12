package com.reservation.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// StaffDTO.java
@Getter
@Setter
@NoArgsConstructor
public class StaffDTO {
    private Long id;
    private String name;
    private String profilePicture;

    public StaffDTO(Long id, String name, String profilePicture) {
        this.id = id;
        this.name = name;
        this.profilePicture = profilePicture;
    }
}