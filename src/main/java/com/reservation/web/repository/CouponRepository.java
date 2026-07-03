package com.reservation.web.repository;

import com.reservation.web.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponRepository extends JpaRepository<CouponEntity, Long> {
    List<CouponEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    List<CouponEntity> findAllByOrderByCreatedAtDesc();
}
