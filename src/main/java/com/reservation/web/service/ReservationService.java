package com.reservation.web.service;

import com.reservation.web.entity.CourseEntity;
import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.repository.CourseRepository;
import com.reservation.web.repository.ReservationRepository; // Repository가 String ID를 다루도록 정의되어야 함
import com.reservation.web.dto.ReservationDTO; // DTO 임포트
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // Optional 사용
import java.util.stream.Collectors; // List<Entity> -> List<DTO> 변환 시 필요

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final CourseRepository courseRepository; // CourseRepository 의존성

    private final int MAX_RESERVATIONS_PER_USER = 2;
    // 코스 durationMinutes를 사용하거나 고정 값을 사용
    private final int BLOCK_MINUTES = 30; // 예시

    public ReservationService(ReservationRepository reservationRepository, CourseRepository courseRepository) {
        this.reservationRepository = reservationRepository;
        this.courseRepository = courseRepository;
    }

    // DTO를 받아서 Entity로 변환하고 저장
    @Transactional
    public ReservationEntity saveReservation(ReservationDTO reservationDTO) {
        // DTO 유효성 검사는 컨트롤러 또는 서비스 앞단에서 수행 가능

        // 1. ReservationDTO의 courseId를 사용하여 CourseEntity 객체를 DB에서 조회
        Long courseId = reservationDTO.getCourseId();
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 코스 ID: " + courseId)); // 코스가 없으면 예외 발생

        // 2. 새로운 ReservationEntity 객체 생성
        ReservationEntity reservationEntity = new ReservationEntity();

        // 3. DTO에서 받은 정보를 Entity에 설정
        // reservationEntity.setId(...) // @GeneratedValue와 @UuidGenerator 사용 시 ID 설정 필요 없음
        reservationEntity.setUserId(reservationDTO.getUserId()); // 회원 예약일 경우
        reservationEntity.setName(reservationDTO.getName()); // 비회원 예약일 경우
        reservationEntity.setPhoneNumber(reservationDTO.getPhoneNumber()); // 비회원 예약일 경우
        reservationEntity.setReservationDateTime(reservationDTO.getReservationDateTime());

        // CourseEntity 객체를 ReservationEntity에 설정 (외래 키 제약 만족)
        reservationEntity.setCourse(course);

        // 4. 예약 상태 설정 (처음 생성 시 PENDING)
        reservationEntity.setStatus("PENDING"); // 또는 다른 기본 상태

        // 5. 예약 로직 수행 (예: 회원당 최대 예약 수 확인, 시간 중복 확인)
        if (reservationEntity.getUserId() != null && !reservationEntity.getUserId().trim().isEmpty()) { // userId가 null이 아니면 회원 예약
            int confirmedCount = reservationRepository.countByUserIdAndStatus(reservationEntity.getUserId(), "CONFIRMED");
            if (confirmedCount >= MAX_RESERVATIONS_PER_USER) {
                throw new IllegalStateException("회원당 최대 " + MAX_RESERVATIONS_PER_USER + "개의 확정된 예약만 가능합니다.");
            }
        }

        // 예약 시간 차단 확인 (동일 시간대 예약 방지 로직)
        // TODO: 코스별 시간 차단이 필요하다면 courseId 파라미터 추가 및 로직 수정
        if (!isTimeSlotAvailable(reservationEntity.getReservationDateTime())) {
            throw new IllegalStateException("해당 시간대는 이미 예약이 불가능합니다.");
        }

        // 6. Entity 저장
        return reservationRepository.save(reservationEntity);
    }

    // ID로 예약을 찾는 메소드 (ID 타입 String)
    public Optional<ReservationEntity> findById(String id) { // 파라미터 String으로 변경
        return reservationRepository.findById(id); // Repository 호출도 String ID에 맞게 동작
    }

    // 예약 정보 업데이트 메소드 (ID 타입 String, DTO 받아서 처리)
    @Transactional
    public ReservationEntity updateReservation(String id, ReservationDTO updatedReservationDTO) { // ID 파라미터 String으로 변경
        ReservationEntity existingReservation = reservationRepository.findById(id) // findById 파라미터 String으로 변경
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 예약 ID: " + id));

        // DTO에서 받은 정보로 Entity 업데이트
        existingReservation.setReservationDateTime(updatedReservationDTO.getReservationDateTime());
        // 상태 변경은 여기서 DTO에서 받아 처리
        existingReservation.setStatus(updatedReservationDTO.getStatus());


        // 코스 변경이 DTO에 포함되어 있다면 처리
        // DTO의 courseId가 기존 Entity의 Course ID와 다르거나, 기존 Course가 null인 경우
        if (updatedReservationDTO.getCourseId() != null &&
                (existingReservation.getCourse() == null || !updatedReservationDTO.getCourseId().equals(existingReservation.getCourse().getId()))) {
            CourseEntity newCourse = courseRepository.findById(updatedReservationDTO.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 코스 ID: " + updatedReservationDTO.getCourseId()));
            existingReservation.setCourse(newCourse); // 변경된 코스 Entity 설정
        } else if (updatedReservationDTO.getCourseId() == null && existingReservation.getCourse() != null) {
            // DTO에서 courseId를 null로 보냈는데 기존 Entity에 코스가 있다면 (필요에 따라) 관계 제거
            existingReservation.setCourse(null); // 단, DB 외래 키가 nullable=false이면 오류 발생 가능
        }


        // DTO에서 이름, 전화번호, 사용자 ID 등을 받아 업데이트 (null이 아닌 경우에만 업데이트 고려)
        if (updatedReservationDTO.getName() != null) existingReservation.setName(updatedReservationDTO.getName());
        if (updatedReservationDTO.getPhoneNumber() != null) existingReservation.setPhoneNumber(updatedReservationDTO.getPhoneNumber());
        if (updatedReservationDTO.getUserId() != null) existingReservation.setUserId(updatedReservationDTO.getUserId()); // 회원 ID 변경 로직 필요 시 확인

        return reservationRepository.save(existingReservation);
    }

    // 예약 삭제 메소드 (ID 타입 String)
    @Transactional
    public void deleteReservation(String id) { // 파라미터 String으로 변경
        // 삭제할 예약을 찾아서 없으면 예외 발생 (findById 재활용)
        reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 예약이 존재하지 않습니다. ID: " + id));

        reservationRepository.deleteById(id); // Repository 호출도 String ID에 맞게 동작
    }


    // 다른 메소드들 (ID 타입 String, String userId/phoneNumber 등을 사용하는 메소드는 그대로 유지 가능)
    // 단, Repository 메소드들이 ReservationEntity의 String ID를 올바르게 처리하도록 수정되었는지 확인 필요
    public List<ReservationEntity> findByStatus(String status) {
        return reservationRepository.findByStatus(status);
    }

    public List<ReservationEntity> findByPhoneNumber(String phoneNumber) {
        return reservationRepository.findByPhoneNumber(phoneNumber);
    }

    public int countConfirmedReservations(String userId) {
        return reservationRepository.countByUserIdAndStatus(userId, "CONFIRMED");
    }

    // 예약 시간 차단 확인 (동일 시간대 예약 방지 로직)
    // TODO: 코스별 시간 차단이 필요하다면 courseId 파라미터 추가 및 로직 수정
    public boolean isTimeSlotAvailable(LocalDateTime reservationDateTime) {
        LocalDateTime start = reservationDateTime.minusMinutes(BLOCK_MINUTES);
        LocalDateTime end = reservationDateTime.plusMinutes(BLOCK_MINUTES);
        List<ReservationEntity> conflictingReservations = reservationRepository.findByReservationDateTimeBetween(start, end);
        return conflictingReservations.isEmpty();
    }

    public void blockReservationTime(LocalDateTime blockTime) {
        // 예약 불가 시간 추가 로직 (필요 시)
    }

    public List<ReservationEntity> findByUserId(String userId) {
        return reservationRepository.findByUserId(userId);
    }

    // 모든 예약을 찾아서 반환 (Entity 리스트)
    public List<ReservationEntity> findAll() {
        return reservationRepository.findAll();
    }

    // Entity 리스트를 DTO 리스트로 변환하는 헬퍼 메소드 (컨트롤러에서 필요 시 사용)
    public List<ReservationDTO> convertToDtoList(List<ReservationEntity> entityList) {
        return entityList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ReservationDTO convertToDto(ReservationEntity entity) {
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
}