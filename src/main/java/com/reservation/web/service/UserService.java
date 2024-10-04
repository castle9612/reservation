package com.reservation.web.service;

import com.reservation.web.dto.UserDTO;
import com.reservation.web.entity.UserEntity;
import com.reservation.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository; // jpa, MySQL dependency 추가
    private final PasswordEncoder passwordEncoder;

    public void signup(UserDTO userDTO) {
        // 비밀번호를 암호화합니다.
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);

        // DTO를 Entity로 변환하여 저장합니다.
        UserEntity userEntity = UserEntity.toUserEntity(userDTO);
        userRepository.save(userEntity);
    }

    public UserDTO login(String userID, String password) {

        Optional<UserEntity> userEntityOptional = userRepository.findById(userID);

        if (userEntityOptional.isPresent()) {
            UserEntity userEntity = userEntityOptional.get();

            // 비밀번호가 일치하는지 확인
            if (passwordEncoder.matches(password, userEntity.getPassword())) {
                return UserDTO.toUserDTO(userEntity); // 비밀번호 일치 시 DTO로 반환
            }
        }

        return null; // 로그인 실패 시 null 반환
    }
}