package com.reservation.web.controller;

import com.reservation.web.dto.UserDTO;
import com.reservation.web.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/signup")
    public String showSignupForm(Model model, HttpServletRequest request) {
        model.addAttribute("userDTO", new UserDTO());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@ModelAttribute UserDTO userDTO, RedirectAttributes redirectAttributes) {
        try {
            userService.signup(userDTO);
            return "redirect:/login?signupSuccess";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("signupErrorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("userDTO", userDTO);
            return "redirect:/signup";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(
            HttpServletRequest request,
            Model model
    ) {
        String error = request.getParameter("error");
        String signupSuccess = request.getParameter("signupSuccess");

        if (error != null) {
            String errorMessage = (String) request.getSession().getAttribute("errorMessage");
            if (errorMessage == null) {
                errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
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
