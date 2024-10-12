package com.reservation.web.controller;

import com.reservation.web.dto.UserMyPageDTO;
import com.reservation.web.service.UserMyPageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/userdetails")
public class AdminUserDetailsController {

    private final UserMyPageService myPageService;

    public AdminUserDetailsController(UserMyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String viewUserDetails(@RequestParam String userId, Model model) {
        UserMyPageDTO userDetails = myPageService.getUserDetailsById(userId);
        model.addAttribute("userDetails", userDetails);
        return "admin/userdetails";
    }
}
