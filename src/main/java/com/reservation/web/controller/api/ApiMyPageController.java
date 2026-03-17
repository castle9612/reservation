package com.reservation.web.controller.api;

import com.reservation.web.dto.UserMyPageDTO;
import com.reservation.web.service.UserMyPageService;
import com.reservation.web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class ApiMyPageController {

    private final UserMyPageService userMyPageService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserMyPageDTO> myPage() {
        return ApiResponse.ok(userMyPageService.getUserDetails(userService.getCurrentUserId()));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> updateMyPage(@RequestBody UserMyPageDTO dto) {
        userMyPageService.updateUserInfo(userService.getCurrentUserId(), dto);
        return ApiResponse.ok(null, "내 정보가 수정되었습니다.");
    }
}
