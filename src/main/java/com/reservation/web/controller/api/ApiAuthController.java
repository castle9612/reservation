package com.reservation.web.controller.api;

import com.reservation.web.common.ApiResponse;
import com.reservation.web.dto.UserDTO;
import com.reservation.web.entity.UserEntity;
import com.reservation.web.repository.UserRepository;
import com.reservation.web.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody UserDTO userDTO) {
        userService.signup(userDTO);
        return ResponseEntity.ok(ApiResponse.ok("회원가입이 완료되었습니다.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> me() {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }

        UserEntity userEntity = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        UserDTO userDTO = UserDTO.toUserDTO(userEntity);
        userDTO.setPassword(null);
        return ResponseEntity.ok(ApiResponse.ok("현재 사용자 조회 성공", userDTO));
    }
}
