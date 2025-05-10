package com.reservation.web.controller;

import com.reservation.web.dto.UserDTO; // UserDTO 임포트
import com.reservation.web.service.UserService;
import jakarta.servlet.http.HttpServletRequest; // HttpServletRequest 임포트
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Model 임포트
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 회원가입 페이지
    @GetMapping("/signup")
    public String showSignupForm(Model model, HttpServletRequest request) { // Model 파라미터 추가
        // "userDTO"라는 이름으로 빈 UserDTO 객체를 모델에 추가
        model.addAttribute("userDTO", new UserDTO());
        return "signup";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String registerUser(@ModelAttribute UserDTO userDTO) { // 폼 데이터를 UserDTO로 받음
        userService.signup(userDTO);
        return "redirect:/login?signupSuccess"; // 회원가입 성공 시 성공 파라미터와 함께 로그인 페이지로 리다이렉션
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "signupSuccess", required = false) String signupSuccess, // 회원가입 성공 파라미터 추가
                                HttpServletRequest request,
                                Model model) {
        if (error != null) {
            String errorMessage = (String) request.getSession().getAttribute("errorMessage");
            if (errorMessage == null) { // CustomAuthFailureHandler에서 메시지를 세션에 저장하지 않은 경우 대비
                errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다."; // 기본 에러 메시지
            }
            model.addAttribute("errorMessage", errorMessage);
            request.getSession().removeAttribute("errorMessage");
        }
        if (signupSuccess != null) {
            model.addAttribute("signupSuccessMessage", "회원가입이 완료되었습니다. 로그인해주세요.");
        }
        return "login";
    }

}