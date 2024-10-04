package com.reservation.web.controller;

import com.reservation.web.dto.UserDTO;
import com.reservation.web.entity.UserEntity;
import com.reservation.web.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 회원가입 페이지 출력 요청 - GetMapping으로 출력 요청 -> PostMapping에서 form에 대한 action 수행
    @GetMapping("/signup")
    public String saveForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String join(@ModelAttribute UserDTO userDTO) {
        System.out.println("UserController.save");
        System.out.println("userDTO = " + userDTO);
        userService.signup(userDTO);
        return "index";
    }

    // 로그인 페이지 출력
    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Invalid username or password.");
        }
        return "login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam String userID,
                        @RequestParam String pwd,
                        HttpSession session,
                        Model model) {
        UserDTO loginUser = userService.login(userID, pwd);
        if (loginUser != null) {
            // 로그인 성공 시 세션에 사용자 정보 저장
            session.setAttribute("loginUser", loginUser);
            return "index"; // 로그인 성공 후 메인 페이지로 이동
        } else {
            // 로그인 실패 시 로그인 페이지로 다시 이동하며, 에러 메시지 전달
            model.addAttribute("loginError", "Incorrect ID or password. Please try again.");
            return "login";
        }
    }

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return "redirect:/login"; // 로그아웃 후 로그인 페이지로 리다이렉트
    }
}