package com.reservation.web.controller;

import com.reservation.web.entity.UserEntity;
import com.reservation.web.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new UserEntity());
        return "signup"; // 회원가입 페이지
    }

    @PostMapping("/signup")
    public String signup(UserEntity user) {
        System.out.println(user);
        System.out.println("Before encoding: " + user.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println("After encoding: " + user.getPassword());
        userService.save(user);
        return "redirect:/login"; // 로그인 페이지로 리다이렉트
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // 로그인 페이지
    }

    @PostMapping("/login")
    public String login(String username, String password, Model model) {
        System.out.println("Attempting to log in user: " + username); // 추가된 로그
        UserEntity user = userService.findByUsername(username);
        if (user != null) {
            System.out.println("User found: " + user.getUsername()); // 사용자 확인
            System.out.println("Stored password: " + user.getPassword()); // 저장된 비밀번호 출력
            if (passwordEncoder.matches(password, user.getPassword())) {
                // 성공적인 로그인 처리 로직
                return "redirect:/announcements"; // 공지사항 페이지로 리다이렉트
            } else {
                System.out.println("Password does not match."); // 비밀번호 불일치 로그
            }
        } else {
            System.out.println("User not found."); // 사용자 미발견 로그
        }
        model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
        return "login"; // 다시 로그인 페이지로 이동
    }
}
