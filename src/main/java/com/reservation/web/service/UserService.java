package com.reservation.web.service;

import com.reservation.web.dto.UserDTO;
import com.reservation.web.entity.UserEntity;
import com.reservation.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 로직
    public void signup(UserDTO userDTO) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);

        // DTO를 Entity로 변환 후 저장
        UserEntity userEntity = UserEntity.toUserEntity(userDTO);
        userRepository.save(userEntity);
    }

    // 로그인 로직
    public UserDTO login(String userID, String password) {
        Optional<UserEntity> userEntityOptional = userRepository.findById(userID);

        if (userEntityOptional.isPresent()) {
            UserEntity userEntity = userEntityOptional.get();

            // 비밀번호 일치 여부 확인
            if (passwordEncoder.matches(password, userEntity.getPassword())) {
                return UserDTO.toUserDTO(userEntity); // 일치하면 DTO로 반환
            }
        }

        return null; // 로그인 실패 시 null 반환
    }

    // 현재 로그인된 사용자의 ID를 반환하는 메소드
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;  // 인증되지 않은 경우
        }
        return authentication.getName();  // 인증된 사용자 이름 반환
    }
}
