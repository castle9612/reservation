package com.reservation.web.controller.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthStatusResponse {
    private boolean authenticated;
    private String userId;
    private String role;
}