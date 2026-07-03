package com.reservation.web.service;

import com.reservation.web.entity.CouponEntity;
import com.reservation.web.repository.CouponRepository;
import com.reservation.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CouponEntity> findAll() {
        return couponRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<CouponEntity> findByUserId(String userId) {
        return couponRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public CouponEntity issueSignupCoupon(String userId) {
        return issueCoupon(userId, "회원가입 축하 쿠폰", 5000, "AVAILABLE", null);
    }

    @Transactional
    public CouponEntity issueCoupon(String userId, String name, Integer discountAmount, String status, LocalDateTime expiresAt) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("쿠폰을 발급할 회원을 찾을 수 없습니다: " + userId);
        }

        CouponEntity coupon = new CouponEntity();
        apply(coupon, userId, name, discountAmount, status, expiresAt);
        return couponRepository.save(coupon);
    }

    @Transactional
    public CouponEntity updateCoupon(Long id, String userId, String name, Integer discountAmount, String status, LocalDateTime expiresAt) {
        CouponEntity coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + id));
        apply(coupon, userId, name, discountAmount, status, expiresAt);
        return couponRepository.save(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 쿠폰을 찾을 수 없습니다: " + id);
        }
        couponRepository.deleteById(id);
    }

    @Transactional
    public CouponEntity useCoupon(Long couponId, String userId) {
        if (couponId == null) {
            return null;
        }

        CouponEntity coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        if (!userId.equals(coupon.getUserId())) {
            throw new IllegalArgumentException("해당 회원의 쿠폰이 아닙니다.");
        }
        if (!"AVAILABLE".equalsIgnoreCase(coupon.getStatus())) {
            throw new IllegalStateException("사용 가능한 쿠폰이 아닙니다.");
        }
        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            coupon.setStatus("EXPIRED");
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }

        coupon.setStatus("USED");
        return couponRepository.save(coupon);
    }

    private void apply(CouponEntity coupon, String userId, String name, Integer discountAmount, String status, LocalDateTime expiresAt) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("쿠폰을 받을 회원을 찾을 수 없습니다: " + userId);
        }
        coupon.setUserId(userId);
        coupon.setName(name == null || name.isBlank() ? "쿠폰" : name.trim());
        coupon.setDiscountAmount(Math.max(0, discountAmount == null ? 0 : discountAmount));
        coupon.setStatus(normalizeStatus(status));
        coupon.setExpiresAt(expiresAt);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "AVAILABLE";
        }
        return switch (status.trim().toUpperCase()) {
            case "USED" -> "USED";
            case "EXPIRED" -> "EXPIRED";
            default -> "AVAILABLE";
        };
    }
}
