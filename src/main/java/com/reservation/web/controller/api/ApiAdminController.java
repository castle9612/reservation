package com.reservation.web.controller.api;

import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.entity.UserEntity;
import com.reservation.web.repository.ReservationRepository;
import com.reservation.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class ApiAdminController {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> users() {
        List<Map<String, Object>> data = userRepository.findAll()
                .stream()
                .map(this::toUserMap)
                .toList();
        return ApiResponse.ok(data);
    }

    @GetMapping("/reservations")
    public ApiResponse<List<Map<String, Object>>> reservations() {
        List<Map<String, Object>> data = reservationRepository.findAll()
                .stream()
                .map(this::toReservationMap)
                .toList();
        return ApiResponse.ok(data);
    }

    private Map<String, Object> toUserMap(UserEntity user) {
        return Map.of(
                "userId", user.getUserId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber(),
                "role", user.getRole()
        );
    }

    private Map<String, Object> toReservationMap(ReservationEntity reservation) {
        return Map.of(
                "id", reservation.getId(),
                "userId", reservation.getUserId() == null ? "" : reservation.getUserId(),
                "name", reservation.getName() == null ? "" : reservation.getName(),
                "status", reservation.getStatus() == null ? "" : reservation.getStatus(),
                "reservationDateTime", reservation.getReservationDateTime()
        );
    }
}
