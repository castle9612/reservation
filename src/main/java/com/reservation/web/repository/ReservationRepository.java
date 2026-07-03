package com.reservation.web.repository;

import com.reservation.web.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, String> {
    @Query("SELECT r FROM ReservationEntity r LEFT JOIN FETCH r.course")
    List<ReservationEntity> findAllWithCourse();

    List<ReservationEntity> findByUserId(String userId);
    List<ReservationEntity> findByStatus(String status);
    List<ReservationEntity> findByReservationDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<ReservationEntity> findByReservationDateTimeBetweenAndStatusIn(LocalDateTime start, LocalDateTime end, Collection<String> statuses);
    List<ReservationEntity> findByPhoneNumberAndName(String phoneNumber, String name);
    List<ReservationEntity> findByCourse_Id(Long courseId);
    void deleteByCourse_Id(Long courseId);
    @Query("SELECT r.status, COUNT(r) FROM ReservationEntity r WHERE r.userId = :userId GROUP BY r.status")
    List<Object[]> countStatusesByUserId(@Param("userId") String userId);
    int countByUserIdAndStatus(String userId, String status);
    int countByUserIdAndStatusIn(String userId, Collection<String> statuses);
}
