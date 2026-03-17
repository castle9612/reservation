package com.reservation.web.controller;

import com.reservation.web.dto.UserDTO;
import com.reservation.web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/signup")
    public String showSignupForm() {
        return "forward:/react-app/index.html";
    }

    @PostMapping("/signup")
    public String registerUser(@ModelAttribute UserDTO userDTO,
                               RedirectAttributes redirectAttributes) {
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
    public String showLoginForm() {
        return "forward:/react-app/index.html";
    }

    @GetMapping({"/", "/index"})
    public String index() {
        return "forward:/react-app/index.html";
    }
}
