package com.reservation.web.controller;

import com.reservation.web.dto.AnnouncementDTO;
import com.reservation.web.entity.AnnouncementEntity;
import com.reservation.web.service.AnnouncementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AnnouncementController {
    private static final Logger logger = LoggerFactory.getLogger(AnnouncementController.class);
    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("/announcement/list")
    public String listAnnouncements(Model model) {
        List<AnnouncementEntity> announcements = announcementService.findAll();
        model.addAttribute("announcements", announcements);
        return "announcement/list";
    }

    @GetMapping("/announcement/detail/{id}")
    public String viewAnnouncement(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        AnnouncementEntity announcement = announcementService.findById(id);
        if (announcement == null) {
            logger.warn("Announcement not found with id: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "요청하신 공지사항을 찾을 수 없습니다 (ID: " + id + ")");
            return "redirect:/announcement/list";
        }
        model.addAttribute("announcement", announcement);
        // Entity에 originalAttachmentNames가 있으므로, detail.html에서 직접 사용 가능
        return "announcement/detail";
    }

    @Controller
    @RequestMapping("/admin/announcements")
    @PreAuthorize("hasRole('ADMIN')")
    public static class AdminAnnouncementController {
        private static final Logger adminLogger = LoggerFactory.getLogger(AdminAnnouncementController.class);
        private final AnnouncementService announcementService;

        public AdminAnnouncementController(AnnouncementService announcementService) {
            this.announcementService = announcementService;
        }

        @GetMapping
        public String adminListAnnouncements(Model model) {
            model.addAttribute("announcements", announcementService.findAll());
            return "announcement/list";
        }

        @GetMapping("/new")
        public String showCreateForm(Model model) {
            if (!model.containsAttribute("announcementDTO")) {
                model.addAttribute("announcementDTO", new AnnouncementDTO());
            }
            return "admin/announcement_form";
        }

        @PostMapping
        public String createAnnouncement(@Valid @ModelAttribute("announcementDTO") AnnouncementDTO announcementDTO,
                                         BindingResult bindingResult,
                                         @RequestParam(name = "newAttachmentFiles", required = false) MultipartFile[] newAttachmentFiles,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
            adminLogger.info("Attempting to create announcement. Title: {}", announcementDTO.getTitle());
            if (bindingResult.hasErrors()) {
                adminLogger.warn("Validation errors while creating announcement: {}", bindingResult.getAllErrors());
                // DTO는 @ModelAttribute로 이미 모델에 추가됨
                return "admin/announcement_form";
            }
            try {
                AnnouncementEntity announcement = new AnnouncementEntity();
                announcement.setTitle(announcementDTO.getTitle());
                announcement.setContent(announcementDTO.getContent());
                announcementService.save(announcement, newAttachmentFiles);
                redirectAttributes.addFlashAttribute("successMessage", "공지사항이 성공적으로 등록되었습니다.");
                adminLogger.info("Announcement created successfully. ID: {}", announcement.getId());
                return "redirect:/announcement/list";
            } catch (IOException e) {
                adminLogger.error("IOException while creating announcement", e);
                model.addAttribute("errorMessage", "공지사항 등록 중 파일 처리 오류 발생: " + e.getMessage());
                return "admin/announcement_form";
            }
        }

        @GetMapping("/{id}/edit")
        public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
            adminLogger.info("Showing edit form for announcement id: {}", id);
            if (!model.containsAttribute("announcementDTO")) {
                AnnouncementEntity announcementEntity = announcementService.findById(id);
                if (announcementEntity == null) {
                    adminLogger.warn("Announcement not found for edit with id: {}", id);
                    redirectAttributes.addFlashAttribute("errorMessage", "수정할 공지사항을 찾을 수 없습니다 (ID: " + id + ")");
                    return "redirect:/admin/announcements"; // 관리자 목록으로
                }
                AnnouncementDTO dto = new AnnouncementDTO();
                dto.setId(announcementEntity.getId());
                dto.setTitle(announcementEntity.getTitle());
                dto.setContent(announcementEntity.getContent());
                dto.setCreatedAt(announcementEntity.getCreatedAt());
                dto.setAttachmentPaths(announcementEntity.getAttachmentPaths());
                dto.setOriginalAttachmentNames(announcementEntity.getOriginalAttachmentNames()); // 원본 파일명 추가
                model.addAttribute("announcementDTO", dto);
            }
            return "admin/announcement_form";
        }

        @PostMapping("/{id}/edit")
        public String updateAnnouncement(@PathVariable Long id,
                                         @Valid @ModelAttribute("announcementDTO") AnnouncementDTO announcementDTO,
                                         BindingResult bindingResult,
                                         @RequestParam(name = "newAttachmentFiles", required = false) MultipartFile[] newAttachmentFiles,
                                         @RequestParam(name = "deletedAttachmentPaths", required = false) List<String> deletedAttachmentPaths,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
            adminLogger.info("Attempting to update announcement id: {}", id);
            announcementDTO.setId(id); // PathVariable ID를 DTO에 명시적으로 설정

            if (bindingResult.hasErrors()) {
                adminLogger.warn("Validation errors while updating announcement id {}: {}", id, bindingResult.getAllErrors());
                // 유효성 오류 시, 뷰에 필요한 정보를 다시 채워줘야 함
                AnnouncementEntity currentEntity = announcementService.findById(id); // DB에서 최신 정보 조회
                if (currentEntity != null) {
                    announcementDTO.setAttachmentPaths(currentEntity.getAttachmentPaths()); // DTO 업데이트
                    announcementDTO.setOriginalAttachmentNames(currentEntity.getOriginalAttachmentNames()); // DTO 업데이트
                    if (announcementDTO.getCreatedAt() == null) {
                        announcementDTO.setCreatedAt(currentEntity.getCreatedAt());
                    }
                } else { // 혹시나 수정 중 원본이 삭제된 경우
                    redirectAttributes.addFlashAttribute("errorMessage", "수정하려는 공지사항을 찾을 수 없습니다 (ID: " + id + ").");
                    return "redirect:/admin/announcements";
                }
                return "admin/announcement_form";
            }
            try {
                AnnouncementEntity announcementToUpdate = new AnnouncementEntity(); // 업데이트할 데이터만 담을 객체
                announcementToUpdate.setTitle(announcementDTO.getTitle());
                announcementToUpdate.setContent(announcementDTO.getContent());
                // 서비스의 update 메소드에서 id로 기존 엔티티를 찾아 업데이트 함

                announcementService.update(id, announcementToUpdate, newAttachmentFiles, deletedAttachmentPaths);
                redirectAttributes.addFlashAttribute("successMessage", "공지사항이 성공적으로 수정되었습니다.");
                adminLogger.info("Announcement updated successfully. ID: {}", id);
                return "redirect:/announcement/detail/" + id;
            } catch (IllegalArgumentException e) { // ID로 못찾을 때
                adminLogger.warn("Update failed for announcement id {}: {}", id, e.getMessage());
                model.addAttribute("errorMessage", e.getMessage());
                AnnouncementEntity currentEntity = announcementService.findById(id);
                if (currentEntity != null) {
                    announcementDTO.setAttachmentPaths(currentEntity.getAttachmentPaths());
                    announcementDTO.setOriginalAttachmentNames(currentEntity.getOriginalAttachmentNames());
                    if (announcementDTO.getCreatedAt() == null) announcementDTO.setCreatedAt(currentEntity.getCreatedAt());
                }
                return "admin/announcement_form";
            } catch (IOException e) {
                adminLogger.error("IOException while updating announcement id {}", id, e);
                model.addAttribute("errorMessage", "공지사항 수정 중 파일 처리 오류 발생: " + e.getMessage());
                AnnouncementEntity currentEntity = announcementService.findById(id);
                if (currentEntity != null) {
                    announcementDTO.setAttachmentPaths(currentEntity.getAttachmentPaths());
                    announcementDTO.setOriginalAttachmentNames(currentEntity.getOriginalAttachmentNames());
                    if (announcementDTO.getCreatedAt() == null) announcementDTO.setCreatedAt(currentEntity.getCreatedAt());
                }
                return "admin/announcement_form";
            }
        }

        @PostMapping("/{id}/delete")
        public String deleteAnnouncement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
            adminLogger.info("Attempting to delete announcement id: {}", id);
            try {
                announcementService.delete(id); // 서비스의 delete는 내부적으로 파일 삭제도 처리
                redirectAttributes.addFlashAttribute("successMessage", "공지사항이 삭제되었습니다.");
                adminLogger.info("Announcement deleted successfully. ID: {}", id);
            } catch (Exception e) { // 서비스에서 특정 예외를 던지도록 수정 가능 (예: EntityNotFoundException)
                adminLogger.error("Error deleting announcement id {}", id, e);
                redirectAttributes.addFlashAttribute("errorMessage", "공지사항 삭제 중 오류가 발생했습니다: " + e.getMessage());
            }
            return "redirect:/announcement/list";
        }

        @PostMapping("/uploadSummernoteImageFile")
        @ResponseBody
        public ResponseEntity<?> uploadSummernoteImageFile(@RequestParam("file") MultipartFile multipartFile) {
            adminLogger.info("Uploading summernote image file: {}", multipartFile.getOriginalFilename());
            Map<String, String> response = new HashMap<>();
            try {
                String fileUrl = announcementService.storeSummernoteImage(multipartFile);
                response.put("url", fileUrl);
                return ResponseEntity.ok(response);
            } catch (IOException e) {
                adminLogger.error("Failed to upload summernote image file", e);
                response.put("error", "이미지 업로드 실패: " + e.getMessage());
                return ResponseEntity.status(500).body(response);
            }
        }
    }
}