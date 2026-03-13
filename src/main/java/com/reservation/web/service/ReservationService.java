package com.reservation.web.service;

import com.reservation.web.dto.ReservationDTO;
import com.reservation.web.entity.CourseEntity;
import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.repository.CourseRepository;
import com.reservation.web.repository.ReservationRepository;
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
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final Set<String> ACTIVE_STATUSES = Set.of(STATUS_PENDING, STATUS_CONFIRMED);

    private final ReservationRepository reservationRepository;
    private final CourseRepository courseRepository;

    private final int MAX_RESERVATIONS_PER_USER = 2;
    private final int DEFAULT_BLOCK_MINUTES = 30;

    public ReservationService(ReservationRepository reservationRepository, CourseRepository courseRepository) {
        this.reservationRepository = reservationRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public ReservationEntity saveReservation(ReservationDTO reservationDTO) {
        validateReservationRequest(reservationDTO);

        CourseEntity course = getCourse(reservationDTO.getCourseId());

        if (reservationDTO.isMemberReservation()) {
            validateMemberReservationLimit(reservationDTO.getUserId());
            reservationDTO.setName(null);
            reservationDTO.setPhoneNumber(null);
        } else {
            validateGuestReservationFields(reservationDTO);
            reservationDTO.setUserId(null);
        }

        if (!isTimeSlotAvailable(reservationDTO.getReservationDateTime(), course, null)) {
            throw new IllegalStateException("해당 시간대는 이미 예약이 불가능합니다.");
        }

        ReservationEntity reservationEntity = new ReservationEntity();
        reservationEntity.setUserId(reservationDTO.getUserId());
        reservationEntity.setName(trimToNull(reservationDTO.getName()));
        reservationEntity.setPhoneNumber(trimToNull(reservationDTO.getPhoneNumber()));
        reservationEntity.setReservationDateTime(reservationDTO.getReservationDateTime());
        reservationEntity.setCourse(course);
        reservationEntity.setStatus(defaultStatus(reservationDTO.getStatus()));

        return reservationRepository.save(reservationEntity);
    }

    public Optional<ReservationEntity> findById(String id) {
        return reservationRepository.findById(id);
    }

    @Transactional
    public ReservationEntity updateReservation(String id, ReservationDTO updatedReservationDTO) {
        ReservationEntity existingReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 예약 ID: " + id));

        CourseEntity targetCourse = existingReservation.getCourse();
        if (updatedReservationDTO.getCourseId() != null) {
            targetCourse = getCourse(updatedReservationDTO.getCourseId());
        }

        LocalDateTime targetDateTime = updatedReservationDTO.getReservationDateTime() != null
                ? updatedReservationDTO.getReservationDateTime()
                : existingReservation.getReservationDateTime();

        if (!isTimeSlotAvailable(targetDateTime, targetCourse, existingReservation.getId())) {
            throw new IllegalStateException("수정하려는 시간대는 이미 예약이 불가능합니다.");
        }

        existingReservation.setReservationDateTime(targetDateTime);
        existingReservation.setCourse(targetCourse);

        if (updatedReservationDTO.getStatus() != null && !updatedReservationDTO.getStatus().isBlank()) {
            existingReservation.setStatus(updatedReservationDTO.getStatus().trim().toUpperCase());
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

    public List<ReservationEntity> findByPhoneNumber(String phoneNumber) {
        return reservationRepository.findByPhoneNumber(phoneNumber);
    }

    public int countConfirmedReservations(String userId) {
        return reservationRepository.countByUserIdAndStatus(userId, STATUS_CONFIRMED);
    }

    public boolean isTimeSlotAvailable(LocalDateTime reservationDateTime) {
        List<ReservationEntity> conflictingReservations = reservationRepository.findByReservationDateTimeBetweenAndStatusIn(
                reservationDateTime.minusMinutes(DEFAULT_BLOCK_MINUTES),
                reservationDateTime.plusMinutes(DEFAULT_BLOCK_MINUTES),
                ACTIVE_STATUSES
        );
        return conflictingReservations.isEmpty();
    }

    public boolean isTimeSlotAvailable(LocalDateTime reservationDateTime, CourseEntity course, String excludeReservationId) {
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

            CourseEntity candidateCourse = candidate.getCourse();
            int candidateDuration = resolveBlockMinutes(candidateCourse);
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
        // 기존 시그니처 유지. 필요 시 관리자 차단 테이블 추가해서 확장.
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
        dto.setReservationDateTime(entity.getReservationDateTime());
        dto.setStatus(entity.getStatus());
        dto.setName(entity.getName());
        dto.setPhoneNumber(entity.getPhoneNumber());
        return dto;
    }

    private CourseEntity getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 코스 ID: " + courseId));
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
            throw new IllegalArgumentException("예약자명은 비회원 예약 시 필수 항목입니다.");
        }
        if (trimToNull(reservationDTO.getPhoneNumber()) == null) {
            throw new IllegalArgumentException("연락처는 비회원 예약 시 필수 항목입니다.");
        }
    }

    private void validateMemberReservationLimit(String userId) {
        int activeReservationCount = reservationRepository.countByUserIdAndStatusIn(userId, ACTIVE_STATUSES);
        if (activeReservationCount >= MAX_RESERVATIONS_PER_USER) {
            throw new IllegalStateException("회원당 최대 " + MAX_RESERVATIONS_PER_USER + "개의 진행 중 예약만 가능합니다.");
        }
    }

    private int resolveBlockMinutes(CourseEntity course) {
        if (course == null || course.getDurationMinutes() == null || course.getDurationMinutes() <= 0) {
            return DEFAULT_BLOCK_MINUTES;
        }
        return Math.max(course.getDurationMinutes(), DEFAULT_BLOCK_MINUTES);
    }

    private String defaultStatus(String status) {
        if (status == null || status.isBlank()) {
            return STATUS_PENDING;
        }
        return status.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
