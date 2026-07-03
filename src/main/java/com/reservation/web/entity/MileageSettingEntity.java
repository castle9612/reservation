package com.reservation.web.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mileage_settings")
@Getter
@Setter
@NoArgsConstructor
public class MileageSettingEntity {

    @Id
    private Long id = 1L;

    private Double earningRatePercent = 0.0;
}
