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
import java.util.stream.Collectors;

@Controller
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    // 사용자용 공지사항 목록
    @GetMapping("/announcements")
    public String listAnnouncements(Model model) {
        List<AnnouncementEntity> announcements = announcementService.findAll();
        model.addAttribute("announcements", announcements);
        return "announcement/list";
    }

    // 사용자용 공지사항 상세
    @GetMapping("/announcements/{id}")
    public String viewAnnouncement(@PathVariable Long id, Model model) {
        AnnouncementEntity announcement = announcementService.findById(id);
        if (announcement == null) {
            // 간단한 예외 처리 또는 오류 페이지로 리다이렉트
            return "redirect:/announcements?error=Announcement not found";
        }
        model.addAttribute("announcement", announcement);
        return "announcement/detail";
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

        // 관리자용 공지사항 목록 (선택적, 사용자 목록과 동일한 뷰 사용 가능)
        @GetMapping
        public String adminListAnnouncements(Model model) {
            model.addAttribute("announcements", announcementService.findAll());
            // 필요하다면 관리자용 별도 목록 뷰를 사용할 수 있음
            // return "admin/announcement/list_admin";
            return "redirect:/announcements"; // 사용자 목록으로 리다이렉트 또는 위와 같이 별도 뷰
        }

        @GetMapping("/new")
        public String showCreateForm(Model model) {
            // th:object="${announcementDTO}"에 바인딩될 객체
            model.addAttribute("announcementDTO", new AnnouncementDTO());
            return "admin/announcement_form";
        }

        @PostMapping
        public String createAnnouncement(@Valid @ModelAttribute("announcementDTO") AnnouncementDTO announcementDTO,
                                         BindingResult bindingResult,
                                         @RequestParam(name = "attachmentFiles", required = false) MultipartFile[] attachmentFiles,
                                         RedirectAttributes redirectAttributes) {
            if (bindingResult.hasErrors()) {
                // 폼 유효성 검사 오류 시 다시 폼으로
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
            return "redirect:/announcements"; // 성공 시 공지사항 목록으로
        }

        @GetMapping("/{id}/edit")
        public String showEditForm(@PathVariable Long id, Model model) {
            AnnouncementEntity announcementEntity = announcementService.findById(id);
            if (announcementEntity == null) {
                return "redirect:/announcements?error=Announcement not found";
            }
            AnnouncementDTO announcementDTO = new AnnouncementDTO();
            announcementDTO.setId(announcementEntity.getId());
            announcementDTO.setTitle(announcementEntity.getTitle());
            announcementDTO.setContent(announcementEntity.getContent());
            announcementDTO.setCreatedAt(announcementEntity.getCreatedAt());
            announcementDTO.setAttachmentPaths(announcementEntity.getAttachmentPaths());

            model.addAttribute("announcementDTO", announcementDTO);
            return "admin/announcement_form"; // 생성/수정 폼 재활용
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
            return "redirect:/announcements/" + id; // 수정 후 상세 페이지로
        }


        @PostMapping("/{id}/delete")
        public String deleteAnnouncement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
            try {
                announcementService.delete(id);
                redirectAttributes.addFlashAttribute("successMessage", "공지사항이 삭제되었습니다.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "공지사항 삭제 중 오류가 발생했습니다.");
            }
            return "redirect:/announcements";
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