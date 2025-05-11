package com.reservation.web.repository;

import com.reservation.web.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    @Query("SELECT c FROM CourseEntity c LEFT JOIN FETCH c.staff")
    List<CourseEntity> findAllWithStaff();

    @Query("SELECT c FROM CourseEntity c LEFT JOIN FETCH c.staff WHERE c.id = :id")
    Optional<CourseEntity> findByIdWithStaff(Long id);
}