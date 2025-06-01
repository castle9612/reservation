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
        Optional<UserEntity> userEntityOptional = userRepository.findById(userId);

        if (userEntityOptional.isEmpty()) {
            System.out.println("❌ UserDetailsService: 사용자 없음 - " + userId);
            throw new UsernameNotFoundException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        UserEntity userEntity = userEntityOptional.get();
        System.out.println("✅ UserDetailsService: 사용자 찾음 - " + userEntity.getUserId());
        System.out.println("🎯 사용자 권한: " + userEntity.getRole());

        return new User(
                userEntity.getUserId(),
                userEntity.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole()))
        );
    }



    // 현재 로그인된 사용자의 ID를 반환하는 메소드
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return authentication.getName();
    }
}