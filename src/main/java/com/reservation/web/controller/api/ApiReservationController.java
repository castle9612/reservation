package com.reservation.web.controller.api;

import com.reservation.web.common.ApiResponse;
import com.reservation.web.dto.ReservationDTO;
import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.service.ReservationService;
import com.reservation.web.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiReservationController {

    private final ReservationService reservationService;
    private final UserService userService;

    @PostMapping("/reservations/member")
    public ResponseEntity<ApiResponse<ReservationDTO>> createMemberReservation(@Valid @RequestBody ReservationDTO reservationDTO) {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }

        reservationDTO.setUserId(currentUserId);
        reservationDTO.setName(null);
        reservationDTO.setPhoneNumber(null);

        ReservationEntity saved = reservationService.saveReservation(reservationDTO);
        return ResponseEntity.ok(ApiResponse.ok("회원 예약이 등록되었습니다.", reservationService.convertToDto(saved)));
    }

    @PostMapping("/reservations/guest")
    public ResponseEntity<ApiResponse<ReservationDTO>> createGuestReservation(@Valid @RequestBody ReservationDTO reservationDTO) {
        reservationDTO.setUserId(null);
        ReservationEntity saved = reservationService.saveReservation(reservationDTO);
        return ResponseEntity.ok(ApiResponse.ok("비회원 예약이 등록되었습니다.", reservationService.convertToDto(saved)));
    }

    @GetMapping("/reservations/me")
    public ResponseEntity<ApiResponse<List<ReservationDTO>>> myReservations() {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }

        List<ReservationDTO> reservations = reservationService.convertToDtoList(reservationService.findByUserId(currentUserId));
        return ResponseEntity.ok(ApiResponse.ok("내 예약 조회 성공", reservations));
    }

    @GetMapping("/reservations/guest")
    public ResponseEntity<ApiResponse<List<ReservationDTO>>> guestReservations(@RequestParam String phoneNumber) {
        List<ReservationDTO> reservations = reservationService.convertToDtoList(reservationService.findByPhoneNumber(phoneNumber));
        return ResponseEntity.ok(ApiResponse.ok("비회원 예약 조회 성공", reservations));
    }

    @PatchMapping("/admin/reservations/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReservationDTO>> updateStatus(@PathVariable String id, @RequestParam String status) {
        ReservationDTO request = new ReservationDTO();
        request.setStatus(status);
        ReservationEntity updated = reservationService.updateReservation(id, request);
        return ResponseEntity.ok(ApiResponse.ok("예약 상태가 변경되었습니다.", reservationService.convertToDto(updated)));
    }
}
