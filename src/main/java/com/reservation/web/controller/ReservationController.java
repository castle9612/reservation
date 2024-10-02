package com.reservation.web.controller;

import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public String showReservationForm(Model model) {
        model.addAttribute("reservation", new ReservationEntity());
        return "reservation";
    }

    @PostMapping("/reservations")
    public String createReservation(ReservationEntity reservation) {
        reservationService.save(reservation);
        return "redirect:/reservations";
    }
}
