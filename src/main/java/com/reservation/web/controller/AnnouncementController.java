package com.reservation.web.controller;

import com.reservation.web.dto.AnnouncementDTO;
import com.reservation.web.entity.AnnouncementEntity;
import com.reservation.web.service.AnnouncementService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            redirectAttributes.addFlashAttribute("errorMessage", "요청하신 공지사항을 찾을 수 없습니다. (ID: " + id + ")");
            return "redirect:/announcement/list";
        }
        announcement.setContent(announcementService.sanitizeHtml(announcement.getContent()));
        model.addAttribute("announcement", announcement);
        return "announcement/detail";
    }

    @GetMapping("/announcement/attachment/{id}/{index}")
    public ResponseEntity<UrlResource> downloadAttachment(@PathVariable Long id, @PathVariable int index) throws Exception {
        AnnouncementEntity announcement = announcementService.findById(id);
        if (announcement == null) {
            return ResponseEntity.notFound().build();
        }

        if (announcement.getAttachmentPaths() == null || announcement.getOriginalAttachmentNames() == null) {
            return ResponseEntity.notFound().build();
        }

        if (index < 0 || index >= announcement.getAttachmentPaths().size() || index >= announcement.getOriginalAttachmentNames().size()) {
            return ResponseEntity.notFound().build();
        }

        String webPath = announcement.getAttachmentPaths().get(index);
        String originalFileName = announcement.getOriginalAttachmentNames().get(index);

        String storedFileName = Paths.get(webPath).getFileName().toString();
        Path filePath = Paths.get("./uploads").toAbsolutePath().normalize().resolve(storedFileName).normalize();

        UrlResource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(encodedFileName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
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
                return "admin/announcement_form";
            }

            try {
                AnnouncementEntity announcement = new AnnouncementEntity();
                announcement.setTitle(announcementDTO.getTitle());
                announcement.setContent(announcementDTO.getContent());

                announcementService.save(announcement, newAttachmentFiles);

                redirectAttributes.addFlashAttribute("successMessage", "공지사항이 성공적으로 등록되었습니다.");
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
                    redirectAttributes.addFlashAttribute("errorMessage", "수정할 공지사항을 찾을 수 없습니다. (ID: " + id + ")");
                    return "redirect:/admin/announcements";
                }

                AnnouncementDTO dto = new AnnouncementDTO();
                dto.setId(announcementEntity.getId());
                dto.setTitle(announcementEntity.getTitle());
                dto.setContent(announcementEntity.getContent());
                dto.setCreatedAt(announcementEntity.getCreatedAt());
                dto.setAttachmentPaths(announcementEntity.getAttachmentPaths());
                dto.setOriginalAttachmentNames(announcementEntity.getOriginalAttachmentNames());

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
            announcementDTO.setId(id);

            if (bindingResult.hasErrors()) {
                AnnouncementEntity currentEntity = announcementService.findById(id);
                if (currentEntity != null) {
                    announcementDTO.setAttachmentPaths(currentEntity.getAttachmentPaths());
                    announcementDTO.setOriginalAttachmentNames(currentEntity.getOriginalAttachmentNames());
                    if (announcementDTO.getCreatedAt() == null) {
                        announcementDTO.setCreatedAt(currentEntity.getCreatedAt());
                    }
                }
                return "admin/announcement_form";
            }

            try {
                AnnouncementEntity announcementToUpdate = new AnnouncementEntity();
                announcementToUpdate.setTitle(announcementDTO.getTitle());
                announcementToUpdate.setContent(announcementDTO.getContent());

                announcementService.update(id, announcementToUpdate, newAttachmentFiles, deletedAttachmentPaths);

                redirectAttributes.addFlashAttribute("successMessage", "공지사항이 성공적으로 수정되었습니다.");
                return "redirect:/announcement/detail/" + id;
            } catch (IllegalArgumentException e) {
                model.addAttribute("errorMessage", e.getMessage());
                AnnouncementEntity currentEntity = announcementService.findById(id);
                if (currentEntity != null) {
                    announcementDTO.setAttachmentPaths(currentEntity.getAttachmentPaths());
                    announcementDTO.setOriginalAttachmentNames(currentEntity.getOriginalAttachmentNames());
                    if (announcementDTO.getCreatedAt() == null) {
                        announcementDTO.setCreatedAt(currentEntity.getCreatedAt());
                    }
                }
                return "admin/announcement_form";
            } catch (IOException e) {
                adminLogger.error("IOException while updating announcement id {}", id, e);
                model.addAttribute("errorMessage", "공지사항 수정 중 파일 처리 오류 발생: " + e.getMessage());
                AnnouncementEntity currentEntity = announcementService.findById(id);
                if (currentEntity != null) {
                    announcementDTO.setAttachmentPaths(currentEntity.getAttachmentPaths());
                    announcementDTO.setOriginalAttachmentNames(currentEntity.getOriginalAttachmentNames());
                    if (announcementDTO.getCreatedAt() == null) {
                        announcementDTO.setCreatedAt(currentEntity.getCreatedAt());
                    }
                }
                return "admin/announcement_form";
            }
        }

        @PostMapping("/{id}/delete")
        public String deleteAnnouncement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
            adminLogger.info("Attempting to delete announcement id: {}", id);

            try {
                announcementService.delete(id);
                redirectAttributes.addFlashAttribute("successMessage", "공지사항이 삭제되었습니다.");
            } catch (Exception e) {
                adminLogger.error("Error deleting announcement id {}", id, e);
                redirectAttributes.addFlashAttribute("errorMessage", "공지사항 삭제 중 오류가 발생했습니다: " + e.getMessage());
            }

            return "redirect:/announcement/list";
        }

        @PostMapping("/uploadSummernoteImageFile")
        @ResponseBody
        public ResponseEntity<Map<String, Object>> uploadSummernoteImageFile(@RequestParam("file") MultipartFile multipartFile) {
            adminLogger.info("Uploading summernote image file: {}", multipartFile.getOriginalFilename());

            Map<String, Object> response = new HashMap<>();
            try {
                String fileUrl = announcementService.storeSummernoteImage(multipartFile);
                response.put("url", fileUrl);
                return ResponseEntity.ok(response);
            } catch (IOException e) {
                adminLogger.error("Failed to upload summernote image file", e);
                response.put("error", "이미지 업로드 실패: " + e.getMessage());
                return ResponseEntity.internalServerError().body(response);
            }
        }
    }
}
