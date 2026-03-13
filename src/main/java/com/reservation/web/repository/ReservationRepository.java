package com.reservation.web.repository;

import com.reservation.web.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, String> {
    List<ReservationEntity> findByUserId(String userId);
    List<ReservationEntity> findByStatus(String status);
    List<ReservationEntity> findByReservationDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<ReservationEntity> findByReservationDateTimeBetweenAndStatusIn(LocalDateTime start, LocalDateTime end, Collection<String> statuses);
    List<ReservationEntity> findByPhoneNumber(String phoneNumber);
    int countByUserIdAndStatus(String userId, String status);
    int countByUserIdAndStatusIn(String userId, Collection<String> statuses);
}
