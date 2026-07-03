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
    private String description;

    public StaffDTO(Long id, String name, String profilePicture) {
        this.id = id;
        this.name = name;
        this.profilePicture = profilePicture;
    }

    public StaffDTO(Long id, String name, String profilePicture, String description) {
        this.id = id;
        this.name = name;
        this.profilePicture = profilePicture;
        this.description = description;
    }
}
