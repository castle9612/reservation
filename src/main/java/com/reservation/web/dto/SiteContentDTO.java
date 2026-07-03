package com.reservation.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SiteContentDTO {
    private String brandName;
    private String heroEyebrow;
    private String heroTitle;
    private String heroDescription;
    private String heroImagePath;
    private String storeName;
    private String storeAddress;
    private String storePhone;
    private String locationDescription;
}
