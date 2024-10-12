package com.reservation.web.controller;

import com.reservation.web.dto.UserMyPageDTO;
import com.reservation.web.service.UserMyPageService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/mypage")
public class UserMyPageController {

    private final UserMyPageService myPageService;

    public UserMyPageController(UserMyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping
    public String viewMyPage(Model model, Authentication authentication) {
        String username = authentication.getName();
        UserMyPageDTO userDetails = myPageService.getUserDetails(username);
        model.addAttribute("userDetails", userDetails);
        return "mypage";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute UserMyPageDTO userMyPageDTO, Authentication authentication) {
        String username = authentication.getName();
        myPageService.updateUserInfo(username, userMyPageDTO);
        return "redirect:/mypage";
    }

    @GetMapping("/reservations")
    public String viewReservations(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("reservations", myPageService.getUserReservations(username));
        return "myreservations";
    }

    @PostMapping("/reservations/update")
    public String updateReservation(@RequestParam Long reservationId, @RequestParam String newDateTime) {
        myPageService.updateReservation(reservationId, newDateTime);
        return "redirect:/mypage/reservations";
    }

    // 관리자가 유저 메모를 수정하는 API
    @PostMapping("/admin/memo")
    public String updateUserMemo(@RequestParam String userId, @RequestParam String memo) {
        myPageService.updateUserMemo(userId, memo);
        return "redirect:/admin/userdetails?userId=" + userId;
    }
}
