package com.reservation.web.controller.api;

import com.reservation.web.dto.AnnouncementDTO;
import com.reservation.web.dto.StaffDTO;
import com.reservation.web.entity.AnnouncementEntity;
import com.reservation.web.service.AnnouncementService;
import com.reservation.web.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                .map(this::toAnnouncementSummaryItem)
                .toList();

        List<Map<String, Object>> staff = staffService.findAllStaff().stream()
                .limit(5)
                .map(this::toStaffItem)
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("announcements", announcements);
        data.put("staff", staff);
        data.put("staffs", staff);
        return ApiResponse.ok(data);
    }

    @GetMapping("/announcements")
    public ApiResponse<List<AnnouncementDTO>> announcements() {
        List<AnnouncementDTO> data = announcementService.findAll().stream()
                .map(this::toAnnouncementDetail)
                .toList();
        return ApiResponse.ok(data);
    }

    @GetMapping("/announcements/{id}")
    public ApiResponse<AnnouncementDTO> announcement(@PathVariable Long id) {
        AnnouncementEntity entity = announcementService.findById(id);
        if (entity == null) {
            throw new IllegalArgumentException("공지사항을 찾을 수 없습니다.");
        }
        return ApiResponse.ok(toAnnouncementDetail(entity));
    }

    @GetMapping("/staff")
    public ApiResponse<List<Map<String, Object>>> staff() {
        List<Map<String, Object>> data = staffService.findAllStaff().stream()
                .map(this::toStaffItem)
                .toList();
        return ApiResponse.ok(data);
    }

    private Map<String, Object> toAnnouncementSummaryItem(AnnouncementEntity entity) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", entity.getId());
        item.put("title", entity.getTitle());
        item.put("createdAt", entity.getCreatedAt());
        return item;
    }

    private AnnouncementDTO toAnnouncementDetail(AnnouncementEntity entity) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setAttachmentPaths(entity.getAttachmentPaths());
        dto.setOriginalAttachmentNames(entity.getOriginalAttachmentNames());
        return dto;
    }

    private Map<String, Object> toStaffItem(StaffDTO dto) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", dto.getId());
        item.put("name", dto.getName());
        item.put("profilePicture", dto.getProfilePicture());
        return item;
    }
}
