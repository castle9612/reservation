package com.reservation.web.controller;

import com.reservation.web.entity.UserEntity;
import com.reservation.web.repository.UserRepository;
import com.reservation.web.service.UserMyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminUserDetailsController {

    private final UserMyPageService myPageService;
    private final UserRepository userRepository;

    @GetMapping("/admin/userdetails")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewUserDetails(@RequestParam String userId, Model model) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        model.addAttribute("user", user);
        return "admin/user_edit";
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String userList(Model model) {
        List<UserEntity> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/user_list";
    }

    @PostMapping("/admin/userdetails/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(@PathVariable String userId,
                             @RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String phoneNumber,
                             @RequestParam int packageCount,
                             @RequestParam(required = false) String memo,
                             @RequestParam String role) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없음: " + userId));

        user.setName(name);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPackageCount(packageCount);
        user.setMemo(memo);
        user.setRole(role);

        userRepository.save(user);

        return "redirect:/admin/userdetails?userId=" + userId;
    }
}
