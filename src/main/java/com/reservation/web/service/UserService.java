package com.reservation.web.service;

import com.reservation.web.dto.UserDTO;
import com.reservation.web.entity.UserEntity;
import com.reservation.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(UserDTO userDTO) {
        validateSignupRequest(userDTO);

        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);
        userDTO.setRole("USER");
        userDTO.setPhoneNumber(normalizePhoneNumber(userDTO.getPhoneNumber()));

        UserEntity userEntity = UserEntity.toUserEntity(userDTO);
        userRepository.save(userEntity);
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("아이디 또는 비밀번호가 올바르지 않습니다."));

        String normalizedRole = normalizeRole(userEntity.getRole());

        return new User(
                userEntity.getUserId(),
                userEntity.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + normalizedRole))
        );
    }

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return authentication.getName();
    }

    @Transactional(readOnly = true)
    public UserEntity getUserEntity(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public SignupAvailability checkSignupAvailability(String userId, String userEmail, String phoneNumber) {
        Boolean userIdAvailable = null;
        Boolean userEmailAvailable = null;
        Boolean phoneNumberAvailable = null;

        if (!isBlank(userId)) {
            userIdAvailable = !userRepository.existsById(userId.trim());
        }

        if (!isBlank(userEmail)) {
            userEmailAvailable = !userRepository.existsByEmail(normalizeEmail(userEmail));
        }

        if (!isBlank(phoneNumber)) {
            String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);
            if (!normalizedPhoneNumber.isEmpty() && isValidKoreanPhoneNumber(normalizedPhoneNumber)) {
                phoneNumberAvailable = !userRepository.existsByPhoneNumber(normalizedPhoneNumber);
            }
        }

        return new SignupAvailability(userIdAvailable, userEmailAvailable, phoneNumberAvailable);
    }

    private void validateSignupRequest(UserDTO userDTO) {
        if (userDTO == null) {
            throw new IllegalArgumentException("회원가입 정보가 없습니다.");
        }

        if (isBlank(userDTO.getUserId())) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        if (isBlank(userDTO.getPassword())) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        if (isBlank(userDTO.getUserName())) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }

        if (isBlank(userDTO.getUserEmail())) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        if (isBlank(userDTO.getPhoneNumber())) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }

        userDTO.setUserId(userDTO.getUserId().trim());
        userDTO.setUserName(userDTO.getUserName().trim());
        userDTO.setUserEmail(normalizeEmail(userDTO.getUserEmail()));

        String normalizedPhoneNumber = normalizePhoneNumber(userDTO.getPhoneNumber());
        validateKoreanPhoneNumber(normalizedPhoneNumber);
        userDTO.setPhoneNumber(normalizedPhoneNumber);

        if (!Boolean.TRUE.equals(userDTO.getPrivacyConsent())) {
            throw new IllegalArgumentException("개인정보 수집 및 이용 동의가 필요합니다.");
        }

        if (userRepository.existsById(userDTO.getUserId())) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(userDTO.getUserEmail())) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByPhoneNumber(normalizedPhoneNumber)) {
            throw new IllegalStateException("이미 사용 중인 전화번호입니다.");
        }
    }

    private String normalizeRole(String role) {
        if (isBlank(role)) {
            return "USER";
        }

        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        return normalized;
    }

    private void validateKoreanPhoneNumber(String phoneNumber) {
        if (!isValidKoreanPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("올바른 대한민국 전화번호 형식이 아닙니다.");
        }
    }

    private boolean isValidKoreanPhoneNumber(String phoneNumber) {
        String regex = "^(01[016789]\\d{7,8}|02\\d{7,8}|0[3-9]\\d{8,9})$";
        return phoneNumber != null && phoneNumber.matches(regex);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        return phoneNumber.replaceAll("[^0-9]", "");
    }

    private String normalizeEmail(String userEmail) {
        if (userEmail == null) {
            return null;
        }
        return userEmail.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record SignupAvailability(
            Boolean userIdAvailable,
            Boolean userEmailAvailable,
            Boolean phoneNumberAvailable
    ) {
    }
}
