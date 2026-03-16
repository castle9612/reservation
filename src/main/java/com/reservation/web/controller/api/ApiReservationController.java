package com.reservation.web.controller.api;

import com.reservation.web.dto.ReservationDTO;
import com.reservation.web.service.ReservationService;
import com.reservation.web.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ApiReservationController {

    private final ReservationService reservationService;
    private final UserService userService;

    @PostMapping("/member")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> createMemberReservation(@Valid @RequestBody ReservationRequest request) {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null || currentUserId.isBlank()) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        ReservationDTO dto = new ReservationDTO();
        dto.setUserId(currentUserId);
        dto.setCourseId(request.getCourseId());
        dto.setReservationDateTime(request.getReservationDateTime());

        reservationService.saveReservation(dto);
        return ApiResponse.ok(null, "회원 예약이 등록되었습니다.");
    }

    @PostMapping("/guest")
    public ApiResponse<Void> createGuestReservation(@Valid @RequestBody ReservationRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("비회원 예약은 이름이 필요합니다.");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("비회원 예약은 전화번호가 필요합니다.");
        }

        ReservationDTO dto = new ReservationDTO();
        dto.setCourseId(request.getCourseId());
        dto.setReservationDateTime(request.getReservationDateTime());
        dto.setName(request.getName().trim());
        dto.setPhoneNumber(request.getPhoneNumber().replaceAll("[^0-9]", ""));

        reservationService.saveReservation(dto);
        return ApiResponse.ok(null, "비회원 예약이 등록되었습니다.");
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ReservationDTO>> myReservations() {
        String currentUserId = userService.getCurrentUserId();

        List<ReservationDTO> reservations = reservationService.findByUserId(currentUserId)
                .stream()
                .map(reservationService::convertToDto)
                .toList();

        return ApiResponse.ok(reservations);
    }

    @GetMapping("/search")
    public ApiResponse<List<ReservationDTO>> searchGuestReservations(@RequestParam String phoneNumber) {
        String normalizedPhone = phoneNumber.replaceAll("[^0-9]", "");
        List<ReservationDTO> reservations = reservationService.findByPhoneNumber(normalizedPhone)
                .stream()
                .map(reservationService::convertToDto)
                .toList();

        return ApiResponse.ok(reservations);
    }
}
