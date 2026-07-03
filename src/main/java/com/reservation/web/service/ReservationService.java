package com.reservation.web.service;

import com.reservation.web.dto.ReservationDTO;
import com.reservation.web.entity.CouponEntity;
import com.reservation.web.entity.CourseEntity;
import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.entity.StaffEntity;
import com.reservation.web.repository.CourseRepository;
import com.reservation.web.repository.ReservationRepository;
import com.reservation.web.repository.StaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final Set<String> ACTIVE_STATUSES = Set.of(STATUS_PENDING, STATUS_CONFIRMED);

    private final ReservationRepository reservationRepository;
    private final CourseRepository courseRepository;
    private final StaffRepository staffRepository;
    private final CouponService couponService;
    private final MileageService mileageService;

    private final int maxReservationsPerUser = 2;
    private final int defaultBlockMinutes = 30;

    public ReservationService(ReservationRepository reservationRepository,
                              CourseRepository courseRepository,
                              StaffRepository staffRepository,
                              CouponService couponService,
                              MileageService mileageService) {
        this.reservationRepository = reservationRepository;
        this.courseRepository = courseRepository;
        this.staffRepository = staffRepository;
        this.couponService = couponService;
        this.mileageService = mileageService;
    }

    @Transactional
    public ReservationEntity saveReservation(ReservationDTO reservationDTO) {
        validateReservationRequest(reservationDTO);

        CourseEntity course = getCourse(reservationDTO.getCourseId());
        StaffEntity staff = resolveStaff(reservationDTO.getStaffId(), course);
        CouponEntity coupon = null;

        if (reservationDTO.isMemberReservation()) {
            validateMemberReservationLimit(reservationDTO.getUserId());
            coupon = couponService.useCoupon(reservationDTO.getCouponId(), reservationDTO.getUserId());
            reservationDTO.setName(null);
            reservationDTO.setPhoneNumber(null);
        } else {
            validateGuestReservationFields(reservationDTO);
            reservationDTO.setUserId(null);
        }

        if (!isTimeSlotAvailable(reservationDTO.getReservationDateTime(), course, staff, null)) {
            throw new IllegalStateException("해당 시간에는 이미 예약이 있어 진행할 수 없습니다.");
        }

        ReservationEntity reservationEntity = new ReservationEntity();
        reservationEntity.setUserId(reservationDTO.getUserId());
        reservationEntity.setName(trimToNull(reservationDTO.getName()));
        reservationEntity.setPhoneNumber(trimToNull(reservationDTO.getPhoneNumber()));
        reservationEntity.setReservationDateTime(reservationDTO.getReservationDateTime());
        reservationEntity.setCourse(course);
        reservationEntity.setStaff(staff);
        reservationEntity.setStatus(defaultStatus(reservationDTO.getStatus()));
        if (coupon != null) {
            reservationEntity.setCouponId(coupon.getId());
            reservationEntity.setCouponName(coupon.getName());
            reservationEntity.setCouponDiscountAmount(coupon.getDiscountAmount());
        }

        return reservationRepository.save(reservationEntity);
    }

    public Optional<ReservationEntity> findById(String id) {
        return reservationRepository.findById(id);
    }

    @Transactional
    public ReservationEntity updateReservation(String id, ReservationDTO updatedReservationDTO) {
        ReservationEntity existingReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 예약 ID입니다: " + id));

        CourseEntity targetCourse = existingReservation.getCourse();
        if (updatedReservationDTO.getCourseId() != null) {
            targetCourse = getCourse(updatedReservationDTO.getCourseId());
        }
        StaffEntity targetStaff = updatedReservationDTO.getStaffId() != null
                ? resolveStaff(updatedReservationDTO.getStaffId(), targetCourse)
                : resolveReservationStaff(existingReservation);

        LocalDateTime targetDateTime = updatedReservationDTO.getReservationDateTime() != null
                ? updatedReservationDTO.getReservationDateTime()
                : existingReservation.getReservationDateTime();

        if (!isTimeSlotAvailable(targetDateTime, targetCourse, targetStaff, existingReservation.getId())) {
            throw new IllegalStateException("수정하려는 시간에는 이미 예약이 있어 진행할 수 없습니다.");
        }

        existingReservation.setReservationDateTime(targetDateTime);
        existingReservation.setCourse(targetCourse);
        existingReservation.setStaff(targetStaff);

        String previousStatus = existingReservation.getStatus();
        if (updatedReservationDTO.getStatus() != null && !updatedReservationDTO.getStatus().isBlank()) {
            existingReservation.setStatus(defaultStatus(updatedReservationDTO.getStatus()));
        }

        boolean memberReservation = existingReservation.getUserId() != null && !existingReservation.getUserId().isBlank();
        if (memberReservation) {
            existingReservation.setName(null);
            existingReservation.setPhoneNumber(null);
        } else {
            if (updatedReservationDTO.getName() != null) {
                existingReservation.setName(trimToNull(updatedReservationDTO.getName()));
            }
            if (updatedReservationDTO.getPhoneNumber() != null) {
                existingReservation.setPhoneNumber(trimToNull(updatedReservationDTO.getPhoneNumber()));
            }
        }

        applyMileageIfCompleted(existingReservation, previousStatus);
        return reservationRepository.save(existingReservation);
    }

    @Transactional
    public void deleteReservation(String id) {
        reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 예약이 존재하지 않습니다. ID: " + id));
        reservationRepository.deleteById(id);
    }

    public List<ReservationEntity> findByStatus(String status) {
        return reservationRepository.findByStatus(status);
    }

    public List<ReservationEntity> findGuestReservations(String name, String phoneNumber) {
        String normalizedName = trimToNull(name);
        String normalizedPhoneNumber = trimToNull(phoneNumber);
        if (normalizedName == null || normalizedPhoneNumber == null) {
            throw new IllegalArgumentException("비회원 예약 조회에는 예약자명과 연락처가 모두 필요합니다.");
        }
        return reservationRepository.findByPhoneNumberAndName(normalizedPhoneNumber, normalizedName);
    }

    public int countConfirmedReservations(String userId) {
        return reservationRepository.countByUserIdAndStatus(userId, STATUS_CONFIRMED);
    }

    public boolean isTimeSlotAvailable(LocalDateTime reservationDateTime) {
        List<ReservationEntity> conflictingReservations = reservationRepository.findByReservationDateTimeBetweenAndStatusIn(
                reservationDateTime.minusMinutes(defaultBlockMinutes),
                reservationDateTime.plusMinutes(defaultBlockMinutes),
                ACTIVE_STATUSES
        );
        return conflictingReservations.isEmpty();
    }

    public boolean isTimeSlotAvailable(LocalDateTime reservationDateTime, CourseEntity course, String excludeReservationId) {
        return isTimeSlotAvailable(reservationDateTime, course, resolveStaff(null, course), excludeReservationId);
    }

    public boolean isTimeSlotAvailable(LocalDateTime reservationDateTime, CourseEntity course, StaffEntity staff, String excludeReservationId) {
        int newReservationDuration = resolveBlockMinutes(course);
        LocalDateTime newStart = reservationDateTime;
        LocalDateTime newEnd = reservationDateTime.plusMinutes(newReservationDuration);

        List<ReservationEntity> candidates = reservationRepository.findByReservationDateTimeBetweenAndStatusIn(
                reservationDateTime.minusMinutes(newReservationDuration),
                reservationDateTime.plusMinutes(newReservationDuration),
                ACTIVE_STATUSES
        );

        for (ReservationEntity candidate : candidates) {
            if (excludeReservationId != null && excludeReservationId.equals(candidate.getId())) {
                continue;
            }

            StaffEntity candidateStaff = resolveReservationStaff(candidate);
            if (!sameStaff(staff, candidateStaff)) {
                continue;
            }

            int candidateDuration = resolveBlockMinutes(candidate.getCourse());
            LocalDateTime candidateStart = candidate.getReservationDateTime();
            LocalDateTime candidateEnd = candidateStart.plusMinutes(candidateDuration);

            boolean overlaps = newStart.isBefore(candidateEnd) && newEnd.isAfter(candidateStart);
            if (overlaps) {
                return false;
            }
        }

        return true;
    }

    public void blockReservationTime(LocalDateTime blockTime) {
        // 관리자 전용 별도 차단 테이블이 필요할 때 확장 예정입니다.
    }

    public List<ReservationEntity> findByUserId(String userId) {
        return reservationRepository.findByUserId(userId);
    }

    public List<ReservationEntity> findAll() {
        return reservationRepository.findAll();
    }

    public List<ReservationDTO> convertToDtoList(List<ReservationEntity> entityList) {
        return entityList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ReservationDTO convertToDto(ReservationEntity entity) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        if (entity.getCourse() != null) {
            dto.setCourseId(entity.getCourse().getId());
        }
        StaffEntity staff = resolveReservationStaff(entity);
        if (staff != null) {
            dto.setStaffId(staff.getId());
            dto.setStaffName(staff.getName());
        }
        dto.setReservationDateTime(entity.getReservationDateTime());
        dto.setStatus(entity.getStatus());
        dto.setName(entity.getName());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setCouponId(entity.getCouponId());
        dto.setCouponName(entity.getCouponName());
        dto.setCouponDiscountAmount(entity.getCouponDiscountAmount());
        dto.setMileageEarned(entity.getMileageEarned());
        return dto;
    }

    public ReservationDTO convertToGuestLookupDto(ReservationEntity entity) {
        ReservationDTO dto = convertToDto(entity);
        dto.setId(null);
        dto.setUserId(null);
        dto.setName(maskName(entity.getName()));
        dto.setPhoneNumber(maskPhoneNumber(entity.getPhoneNumber()));
        return dto;
    }

    private CourseEntity getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 코스 ID입니다: " + courseId));
    }

    private StaffEntity resolveStaff(Long staffId, CourseEntity course) {
        if (staffId != null) {
            return staffRepository.findById(staffId)
                    .orElseThrow(() -> new IllegalArgumentException("선택한 스태프를 찾을 수 없습니다: " + staffId));
        }
        return course == null ? null : course.getStaff();
    }

    private StaffEntity resolveReservationStaff(ReservationEntity reservation) {
        if (reservation == null) {
            return null;
        }
        if (reservation.getStaff() != null) {
            return reservation.getStaff();
        }
        return reservation.getCourse() == null ? null : reservation.getCourse().getStaff();
    }

    private boolean sameStaff(StaffEntity first, StaffEntity second) {
        if (first == null || second == null) {
            return first == second;
        }
        return first.getId() != null && first.getId().equals(second.getId());
    }

    private void applyMileageIfCompleted(ReservationEntity reservation, String previousStatus) {
        if (reservation.getUserId() == null || reservation.getUserId().isBlank()) {
            return;
        }
        if (!STATUS_COMPLETED.equals(reservation.getStatus())) {
            return;
        }
        if (STATUS_COMPLETED.equals(previousStatus) || (reservation.getMileageEarned() != null && reservation.getMileageEarned() > 0)) {
            return;
        }

        int baseAmount = 0;
        if (reservation.getCourse() != null && reservation.getCourse().getMemberPrice() != null) {
            baseAmount = (int) Math.round(reservation.getCourse().getMemberPrice());
        }
        if (baseAmount <= 0 && reservation.getCourse() != null && reservation.getCourse().getDurationMinutes() != null) {
            baseAmount = reservation.getCourse().getDurationMinutes() * 1000;
        }
        baseAmount = Math.max(0, baseAmount - Math.max(0, reservation.getCouponDiscountAmount() == null ? 0 : reservation.getCouponDiscountAmount()));
        int earned = mileageService.earnMileage(reservation.getUserId(), baseAmount);
        reservation.setMileageEarned(earned);
    }

    private void validateReservationRequest(ReservationDTO reservationDTO) {
        if (reservationDTO == null) {
            throw new IllegalArgumentException("예약 정보가 없습니다.");
        }
        if (reservationDTO.getCourseId() == null) {
            throw new IllegalArgumentException("코스는 필수 항목입니다.");
        }
        if (reservationDTO.getReservationDateTime() == null) {
            throw new IllegalArgumentException("예약 희망 일시는 필수 항목입니다.");
        }
    }

    private void validateGuestReservationFields(ReservationDTO reservationDTO) {
        if (trimToNull(reservationDTO.getName()) == null) {
            throw new IllegalArgumentException("비회원 예약에는 예약자명이 필요합니다.");
        }
        if (trimToNull(reservationDTO.getPhoneNumber()) == null) {
            throw new IllegalArgumentException("비회원 예약에는 연락처가 필요합니다.");
        }
    }

    private void validateMemberReservationLimit(String userId) {
        int activeReservationCount = reservationRepository.countByUserIdAndStatusIn(userId, ACTIVE_STATUSES);
        if (activeReservationCount >= maxReservationsPerUser) {
            throw new IllegalStateException("회원은 최대 " + maxReservationsPerUser + "개의 진행 중 예약만 가질 수 있습니다.");
        }
    }

    private int resolveBlockMinutes(CourseEntity course) {
        if (course == null || course.getDurationMinutes() == null || course.getDurationMinutes() <= 0) {
            return defaultBlockMinutes;
        }
        return Math.max(course.getDurationMinutes(), defaultBlockMinutes);
    }

    private String defaultStatus(String status) {
        if (status == null || status.isBlank()) {
            return STATUS_PENDING;
        }
        String normalized = status.trim().toUpperCase();
        return "CANCELLED".equals(normalized) ? "CANCELLED_USER" : normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String maskName(String name) {
        String trimmed = trimToNull(name);
        if (trimmed == null || trimmed.length() <= 1) {
            return trimmed == null ? "" : "*";
        }
        return trimmed.charAt(0) + "*".repeat(Math.max(1, trimmed.length() - 1));
    }

    private String maskPhoneNumber(String phoneNumber) {
        String digits = phoneNumber == null ? "" : phoneNumber.replaceAll("[^0-9]", "");
        if (digits.length() < 7) {
            return "";
        }
        return digits.substring(0, 3) + "****" + digits.substring(digits.length() - 4);
    }
}
