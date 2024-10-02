package com.reservation.web.service;

import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationEntity> findByStatus(String status) {
        return reservationRepository.findByStatus(status);
    }

    public ReservationEntity save(ReservationEntity reservation) {
        return reservationRepository.save(reservation);
    }
}
