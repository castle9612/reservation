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

    public UserMyPageService(UserRepository userRepository, ReservationRepository reservationRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserMyPageDTO getUserDetails(String username) {
        UserEntity user = userRepository.findByName(username).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<ReservationEntity> reservations = reservationRepository.findByUserId(user.getUserId());
        return new UserMyPageDTO(user, reservations);
    }

    @Transactional
    public void updateUserInfo(String username, UserMyPageDTO userMyPageDTO) {
        UserEntity user = userRepository.findByName(username).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setName(userMyPageDTO.getName());
        user.setPhoneNumber(userMyPageDTO.getPhoneNumber());
        user.setEmail(userMyPageDTO.getEmail());
        if (userMyPageDTO.getPassword() != null && !userMyPageDTO.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(userMyPageDTO.getPassword());
            user.setPassword(encodedPassword);
        }
        user.setPackageCount(userMyPageDTO.getPackageCount());
        userRepository.save(user);
    }

    public List<ReservationEntity> getUserReservations(String username) {
        UserEntity user = userRepository.findByName(username).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return reservationRepository.findByUserId(user.getUserId());
    }

    public void updateReservation(String reservationId, String newDateTime) {
        ReservationEntity reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        reservation.setReservationDateTime(LocalDateTime.parse(newDateTime));
        reservationRepository.save(reservation);
    }

    @Transactional
    public void updateUserMemo(String userId, String memo) {
        UserEntity user = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setMemo(memo);
        userRepository.save(user);
    }

    public UserMyPageDTO getUserDetailsById(String userId) {
        UserEntity user = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<ReservationEntity> reservations = reservationRepository.findByUserId(user.getUserId());
        return new UserMyPageDTO(user, reservations);
    }

}
