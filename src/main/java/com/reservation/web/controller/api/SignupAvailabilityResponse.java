package com.reservation.web.controller.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupAvailabilityResponse {
    private Boolean userIdAvailable;
    private Boolean userEmailAvailable;
    private Boolean phoneNumberAvailable;
}
