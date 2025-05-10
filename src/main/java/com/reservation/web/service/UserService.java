package com.reservation.web.service;

import com.reservation.web.dto.UserDTO;
import com.reservation.web.entity.UserEntity;
import com.reservation.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User; // Spring Security의 User
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService { // ⬅️ UserDetailsService 구현 추가

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 로직
    public void signup(UserDTO userDTO) {
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);
        UserEntity userEntity = UserEntity.toUserEntity(userDTO);
        userRepository.save(userEntity);
    }

    // ⬇️ UserDetailsService 인터페이스의 메소드 구현
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // 1. userRepository에서 userId로 UserEntity를 조회합니다.
        //    만약 UserEntity의 ID 필드가 userId 문자열 자체가 아니라면 (예: Long id),
        //    UserRepository에 findByUserId(String userId) 같은 메소드가 필요합니다.
        //    여기서는 userId가 UserEntity의 PK이거나, findByUserId 메소드가 있다고 가정합니다.
        //    만약 UserEntity의 @Id 필드가 userId가 아니라면 UserRepository에 다음 메소드를 추가하세요:
        //    Optional<UserEntity> findByUserId(String userId);

        // 이 예제에서는 UserEntity의 user_id 필드가 유니크하고, 이를 통해 찾는다고 가정합니다.
        // UserEntity의 실제 ID 필드와 조회 방식에 맞춰 수정해야 합니다.
        // 예를 들어, UserEntity의 ID가 Long 타입의 'id'이고, 'userId'라는 별도 문자열 필드가 있다면:
        Optional<UserEntity> userEntityOptional = userRepository.findById(userId); // ❗️ 이 메소드가 UserRepository에 정의되어 있어야 합니다.

        // 만약 UserEntity의 PK가 String 타입의 userId라면:
        // Optional<UserEntity> userEntityOptional = userRepository.findById(userId);

        if (userEntityOptional.isEmpty()) {
            System.out.println("❌ UserDetailsService: 사용자 없음 - " + userId);
            throw new UsernameNotFoundException("아이디 또는 비밀번호가 올바르지 않습니다."); // 예외 메시지는 CustomAuthFailureHandler에서 덮어쓰여집니다.
        }

        UserEntity userEntity = userEntityOptional.get();
        System.out.println("✅ UserDetailsService: 사용자 찾음 - " + userEntity.getUserId());

        // 2. UserDetails 객체를 생성하여 반환합니다.
        //    Spring Security의 User 객체를 사용하거나, UserEntity가 UserDetails를 직접 구현할 수 있습니다.
        //    여기서는 Spring Security의 User 객체를 사용합니다.
        //    권한(authorities)은 예시로 "ROLE_USER"를 부여했습니다. 실제로는 userEntity에서 역할을 가져와야 합니다.
        return new User(userEntity.getUserId(), // 로그인에 사용될 ID
                userEntity.getPassword(), // DB에 저장된 암호화된 비밀번호
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))); // 권한 목록
    }


    // 현재 로그인된 사용자의 ID를 반환하는 메소드
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        // UserDetails 객체를 Principal로 사용하는 경우
        if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        // 단순 문자열 이름을 Principal로 사용하는 경우 (기본 설정)
        return authentication.getName();
    }
}