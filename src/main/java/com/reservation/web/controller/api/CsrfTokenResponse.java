package com.reservation.web.controller.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CsrfTokenResponse {
    private String token;
}