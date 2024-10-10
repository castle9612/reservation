package com.reservation.web.service;

import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;

    private final int MAX_RESERVATIONS_PER_USER = 2;
    private final int BLOCK_MINUTES = 30;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationEntity> findByStatus(String status) {
        return reservationRepository.findByStatus(status);
    }

    public List<ReservationEntity> findByPhoneNumber(String phoneNumber) {
        return reservationRepository.findByPhoneNumber(phoneNumber);
    }

    public int countConfirmedReservations(String  userId) {
        return reservationRepository.countByUserIdAndStatus(userId, "CONFIRMED");
    }

    public boolean isTimeSlotAvailable(LocalDateTime reservationDateTime) {
        LocalDateTime start = reservationDateTime.minusMinutes(BLOCK_MINUTES);
        LocalDateTime end = reservationDateTime.plusMinutes(BLOCK_MINUTES);
        return reservationRepository.findByReservationDateTimeBetween(start, end).isEmpty();
    }

    @Transactional
    public ReservationEntity saveReservation(ReservationEntity reservation) {
        // 회원 예약인 경우
        if (reservation.getUserId() != null) {
            if (countConfirmedReservations(reservation.getUserId()) >= MAX_RESERVATIONS_PER_USER) {
                throw new IllegalStateException("회원당 최대 2개의 예약만 가능합니다.");
            }
        }

        // 예약 시간 차단 확인
        if (!isTimeSlotAvailable(reservation.getReservationDateTime())) {
            throw new IllegalStateException("해당 시간대는 이미 예약이 불가능합니다.");
        }

        reservation.setStatus("PENDING"); // 기본 상태는 예약 대기
        return reservationRepository.save(reservation);
    }

    public ReservationEntity findById(String id) {
        return reservationRepository.findById(id).orElse(null);
    }

    @Transactional
    public ReservationEntity updateReservation(String id, ReservationEntity updatedReservation) {
        ReservationEntity existingReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 예약 ID: " + id));

        existingReservation.setReservationDateTime(updatedReservation.getReservationDateTime());
        existingReservation.setStatus(updatedReservation.getStatus());
        existingReservation.setCourseId(updatedReservation.getCourseId());
        existingReservation.setUserId(updatedReservation.getUserId());
        existingReservation.setName(updatedReservation.getName());
        existingReservation.setPhoneNumber(updatedReservation.getPhoneNumber());

        return reservationRepository.save(existingReservation);
    }

    public void deleteReservation(String id) {
        reservationRepository.deleteById(id);
    }

    // 예약 시간 블록 처리 (추가 로직 필요 시 구현)
    public void blockReservationTime(LocalDateTime blockTime) {
        // 예: 예약 불가 시간 추가 로직
    }

    public List<ReservationEntity> findByUserId(String userId) {
        return reservationRepository.findByUserId(userId);
    }

    public List<ReservationEntity> findAll() {
        return reservationRepository.findAll();
    }
}
