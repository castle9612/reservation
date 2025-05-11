package com.reservation.web.repository;

import com.reservation.web.entity.StaffEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<StaffEntity, Long> {
}
