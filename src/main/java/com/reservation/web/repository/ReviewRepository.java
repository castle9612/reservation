package com.reservation.web.repository;

import com.reservation.web.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findAllByOrderByCreatedAtDesc();
    List<ReviewEntity> findByReviewerUserIdOrderByCreatedAtDesc(String reviewerUserId);
}
