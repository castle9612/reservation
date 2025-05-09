package com.reservation.web.controller;

import com.reservation.web.dto.UserDTO;
import com.reservation.web.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 회원가입 페이지
    @GetMapping("/signup")
    public String showSignupForm(HttpServletRequest request) {
        // 불필요한 수동 검증 제거
        return "signup";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String registerUser(@ModelAttribute UserDTO userDTO) {
        userService.signup(userDTO);

        return "redirect:/login";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Invalid username or password.");
        }
        return "login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam String user_id,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        UserDTO loginUser = userService.login(user_id, password);
        if (loginUser != null) {
            session.setAttribute("loginUser", loginUser);
            return "redirect:/";  // 로그인 성공 후 메인 페이지로 리다이렉트
        } else {
            model.addAttribute("loginError", "Incorrect ID or password. Please try again.");
            return "login";
        }
    }

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // 세션 무효화
        return "redirect:/login";  // 로그아웃 후 로그인 페이지로 리다이렉트
    }
}
