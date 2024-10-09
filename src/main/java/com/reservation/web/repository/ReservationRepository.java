package com.reservation.web.repository;

import com.reservation.web.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    List<ReservationEntity> findByStatus(String status);
    List<ReservationEntity> findByReservationDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<ReservationEntity> findByPhoneNumber(String phoneNumber);
    int countByUserIdAndStatus(Long userId, String status);
}
