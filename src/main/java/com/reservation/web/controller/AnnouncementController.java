package com.reservation.web.controller;

import com.reservation.web.dto.AnnouncementDTO;
import com.reservation.web.entity.AnnouncementEntity;
import com.reservation.web.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid; // @Valid 사용 시
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    // 사용자용 공지사항 목록
    // 요청 URL: /announcement/list
    // 실제 뷰 파일: templates/announcement/list.html
    @GetMapping("/announcement/list")
    public String listAnnouncements(Model model) {
        List<AnnouncementEntity> announcements = announcementService.findAll();
        model.addAttribute("announcements", announcements);
        return "announcement/list"; // "templates/announcement/list.html"을 의미
    }

    // 사용자용 공지사항 상세
    // 요청 URL: /announcement/detail/{id}
    // 실제 뷰 파일: templates/announcement_detail.html (가정)
    @GetMapping("/announcement/detail/{id}")
    public String viewAnnouncement(@PathVariable Long id, Model model) {
        AnnouncementEntity announcement = announcementService.findById(id);
        if (announcement == null) {
            // 간단한 예외 처리 또는 오류 페이지로 리다이렉트
            return "redirect:/announcement/list?error=Announcement not found";
        }
        model.addAttribute("announcement", announcement);
        // 만약 실제 뷰 파일이 "templates/announcement/detail.html" 이라면 "announcement/detail" 로 변경
        return "announcement/detail"; // "templates/announcement_detail.html"을 의미
    }

    // --- 관리자 기능 ---
    @Controller
    @RequestMapping("/admin/announcements")
    @PreAuthorize("hasRole('ADMIN')")
    public static class AdminAnnouncementController {

        private final AnnouncementService announcementService;

        public AdminAnnouncementController(AnnouncementService announcementService) {
            this.announcementService = announcementService;
        }

        // 관리자용 공지사항 목록 -> 사용자용 목록 뷰를 재활용
        // 요청 URL: /admin/announcements
        // 실제 뷰 파일: templates/announcement/list.html
        @GetMapping
        public String adminListAnnouncements(Model model) {
            model.addAttribute("announcements", announcementService.findAll());
            // 사용자용 공지사항 목록 뷰를 반환하도록 수정
            return "announcement/list";
        }

        // 관리자용 공지사항 작성 폼
        // 실제 뷰 파일: templates/admin_announcement_form.html (가정)
        @GetMapping("/new")
        public String showCreateForm(Model model) {
            model.addAttribute("announcementDTO", new AnnouncementDTO());
            // 만약 실제 뷰 파일이 "templates/admin/announcement_form.html" 이라면 "admin/announcement_form" 로 변경
            return "admin/announcement_form"; // "templates/admin_announcement_form.html" 을 의미
        }

        @PostMapping
        public String createAnnouncement(@Valid @ModelAttribute("announcementDTO") AnnouncementDTO announcementDTO,
                                         BindingResult bindingResult,
                                         @RequestParam(name = "attachmentFiles", required = false) MultipartFile[] attachmentFiles,
                                         RedirectAttributes redirectAttributes) {
            if (bindingResult.hasErrors()) {
                // 실제 관리자 폼 뷰 파일 경로에 맞춰 수정
                // 예: "admin/announcement_form" 또는 "admin_announcement_form"
                return "admin/announcement_form";
            }
            try {
                AnnouncementEntity announcement = new AnnouncementEntity();
                announcement.setTitle(announcementDTO.getTitle());
                announcement.setContent(announcementDTO.getContent());
                announcementService.save(announcement, attachmentFiles);
                redirectAttributes.addFlashAttribute("successMessage", "공지사항이 성공적으로 등록되었습니다.");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "공지사항 등록 중 오류 발생: " + e.getMessage());
                return "redirect:/admin/announcements/new"; // 오류 시 새 작성 폼으로
            }
            return "redirect:/announcement/list"; // 성공 시 공지사항 목록으로
        }

        // 관리자용 공지사항 수정 폼
        // 실제 뷰 파일: templates/admin_announcement_form.html (가정, 생성 폼 재활용)
        @GetMapping("/{id}/edit")
        public String showEditForm(@PathVariable Long id, Model model) {
            AnnouncementEntity announcementEntity = announcementService.findById(id);
            if (announcementEntity == null) {
                return "redirect:/announcement/list?error=Announcement not found";
            }
            AnnouncementDTO announcementDTO = new AnnouncementDTO();
            announcementDTO.setId(announcementEntity.getId());
            announcementDTO.setTitle(announcementEntity.getTitle());
            announcementDTO.setContent(announcementEntity.getContent());
            announcementDTO.setCreatedAt(announcementEntity.getCreatedAt());
            announcementDTO.setAttachmentPaths(announcementEntity.getAttachmentPaths());

            model.addAttribute("announcementDTO", announcementDTO);
            // 실제 관리자 폼 뷰 파일 경로에 맞춰 수정
            // 예: "admin/announcement_form" 또는 "admin_announcement_form"
            return "admin/announcement_form";
        }

        @PostMapping("/{id}/edit")
        public String updateAnnouncement(@PathVariable Long id,
                                         @Valid @ModelAttribute("announcementDTO") AnnouncementDTO announcementDTO,
                                         BindingResult bindingResult,
                                         @RequestParam(name = "newAttachmentFiles", required = false) MultipartFile[] newAttachmentFiles,
                                         @RequestParam(name = "deletedAttachmentPaths", required = false) List<String> deletedAttachmentPaths,
                                         RedirectAttributes redirectAttributes) {
            if (bindingResult.hasErrors()) {
                // DTO에 기존 첨부파일 경로 다시 넣어주기 (폼에 표시하기 위함)
                AnnouncementEntity currentEntity = announcementService.findById(id);
                if(currentEntity != null) announcementDTO.setAttachmentPaths(currentEntity.getAttachmentPaths());
                // 실제 관리자 폼 뷰 파일 경로에 맞춰 수정
                // 예: "admin/announcement_form" 또는 "admin_announcement_form"
                return "admin/announcement_form";
            }
            try {
                AnnouncementEntity announcementToUpdate = new AnnouncementEntity();
                announcementToUpdate.setTitle(announcementDTO.getTitle());
                announcementToUpdate.setContent(announcementDTO.getContent());
                announcementService.update(id, announcementToUpdate, newAttachmentFiles, deletedAttachmentPaths);
                redirectAttributes.addFlashAttribute("successMessage", "공지사항이 성공적으로 수정되었습니다.");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "공지사항 수정 중 오류 발생: " + e.getMessage());
                return "redirect:/admin/announcements/" + id + "/edit";
            }
            return "redirect:/announcement/detail/" + id; // 수정 후 상세 페이지로
        }


        @PostMapping("/{id}/delete")
        public String deleteAnnouncement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
            try {
                announcementService.delete(id);
                redirectAttributes.addFlashAttribute("successMessage", "공지사항이 삭제되었습니다.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "공지사항 삭제 중 오류가 발생했습니다.");
            }
            return "redirect:/announcement/list"; // 삭제 후 공지사항 목록으로
        }

        @PostMapping("/uploadSummernoteImageFile")
        @ResponseBody
        public ResponseEntity<?> uploadSummernoteImageFile(@RequestParam("file") MultipartFile multipartFile) {
            Map<String, String> response = new HashMap<>();
            try {
                String fileUrl = announcementService.storeSummernoteImage(multipartFile);
                response.put("url", fileUrl);
                return ResponseEntity.ok(response);
            } catch (IOException e) {
                response.put("error", "이미지 업로드 실패: " + e.getMessage());
                return ResponseEntity.status(500).body(response);
            }
        }
    }
}