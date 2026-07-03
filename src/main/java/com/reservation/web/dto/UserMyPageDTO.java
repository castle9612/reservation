package com.reservation.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserMyPageDTO {
    private String userId;
    private String userName;
    private String phoneNumber;
    private String userEmail;
    private String password;
    private int packageCount;
    private int mileageBalance;
    private String memo;
    private List<CouponSummary> coupons = new ArrayList<>();

    @Getter
    @Setter
    public static class CouponSummary {
        private Long id;
        private String name;
        private Integer discountAmount;
        private String status;
        private LocalDateTime expiresAt;
    }
}
