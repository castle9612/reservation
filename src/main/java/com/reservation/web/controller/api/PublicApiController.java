package com.reservation.web.controller.api;

import com.reservation.web.dto.StaffDTO;
import com.reservation.web.entity.AnnouncementEntity;
import com.reservation.web.service.AnnouncementService;
import com.reservation.web.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicApiController {

    private final AnnouncementService announcementService;
    private final StaffService staffService;

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        List<Map<String, Object>> announcements = announcementService.findAll().stream()
                .limit(5)
                .map(this::toAnnouncementItem)
                .collect(Collectors.toList());

        List<Map<String, Object>> staff = staffService.findAllStaff().stream()
                .map(this::toStaffItem)
                .collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("announcements", announcements);
        data.put("staff", staff);
        return ApiResponse.ok(data);
    }

    @GetMapping("/announcements")
    public ApiResponse<List<Map<String, Object>>> announcements() {
        List<Map<String, Object>> data = announcementService.findAll().stream()
                .map(this::toAnnouncementItem)
                .collect(Collectors.toList());
        return ApiResponse.ok(data);
    }

    @GetMapping("/staff")
    public ApiResponse<List<Map<String, Object>>> staff() {
        List<Map<String, Object>> data = staffService.findAllStaff().stream()
                .map(this::toStaffItem)
                .collect(Collectors.toList());
        return ApiResponse.ok(data);
    }

    private Map<String, Object> toAnnouncementItem(AnnouncementEntity entity) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", entity.getId());
        item.put("title", entity.getTitle());
        item.put("createdAt", entity.getCreatedAt());
        return item;
    }

    private Map<String, Object> toStaffItem(StaffDTO dto) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", dto.getId());
        item.put("name", dto.getName());
        item.put("profilePicture", dto.getProfilePicture());
        return item;
    }
}
