package com.reservation.web.service;

import com.reservation.web.dto.UserMyPageDTO;
import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.entity.UserEntity;
import com.reservation.web.repository.ReservationRepository;
import com.reservation.web.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserMyPageService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserMyPageService(UserRepository userRepository,
                             ReservationRepository reservationRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserMyPageDTO getUserDetails(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        UserMyPageDTO dto = new UserMyPageDTO();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getName());
        dto.setUserEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setPackageCount(user.getPackageCount());
        dto.setMemo(user.getMemo());
        return dto;
    }

    @Transactional
    public void updateUserInfo(String userId, UserMyPageDTO userMyPageDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.setName(userMyPageDTO.getUserName());
        user.setPhoneNumber(userMyPageDTO.getPhoneNumber());
        user.setEmail(userMyPageDTO.getUserEmail());

        if (userMyPageDTO.getPassword() != null && !userMyPageDTO.getPassword().isEmpty()) {
            if (!passwordEncoder.matches(userMyPageDTO.getPassword(), user.getPassword())) {
                user.setPassword(passwordEncoder.encode(userMyPageDTO.getPassword()));
            }
        }

        user.setPackageCount(userMyPageDTO.getPackageCount());
        userRepository.save(user);
    }

    public List<ReservationEntity> getUserReservations(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        return reservationRepository.findByUserId(user.getUserId());
    }

    @Transactional
    public void updateReservation(String reservationId, String newDateTime) {
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다: " + reservationId));
        try {
            reservation.setReservationDateTime(LocalDateTime.parse(newDateTime));
            reservationRepository.save(reservation);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("잘못된 날짜/시간 형식입니다: " + newDateTime, e);
        }
    }

    @Transactional
    public void updateUserMemo(String userId, String memo) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        user.setMemo(memo);
        userRepository.save(user);
    }

    public UserMyPageDTO getUserDetailsById(String userId) {
        return getUserDetails(userId);
    }
}
