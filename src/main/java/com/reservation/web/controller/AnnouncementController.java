package com.reservation.web.controller;

import com.reservation.web.dto.AnnouncementDTO;
import com.reservation.web.entity.AnnouncementEntity;
import com.reservation.web.service.AnnouncementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/admin/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public String getAnnouncements(Model model) {
        model.addAttribute("announcements", announcementService.findAll());
        return "announcement/list";
    }

    @GetMapping("/{id}")
    public String getAnnouncementDetail(@PathVariable Long id, Model model) {
        AnnouncementEntity announcement = announcementService.findById(id);
        model.addAttribute("announcement", announcement);
        return "announcement/detail";
    }

    @GetMapping("/new")
    public String createAnnouncementForm(Model model) {
        model.addAttribute("announcement", new AnnouncementDTO());
        return "announcement/new";
    }

    @PostMapping
    public String saveAnnouncement(@ModelAttribute AnnouncementDTO announcementDTO,
                                   @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setTitle(announcementDTO.getTitle());
        announcement.setContent(announcementDTO.getContent());
        announcementService.save(announcement, imageFile);

        return "redirect:/announcements";
    }

    @GetMapping("/{id}/edit")
    public String editAnnouncementForm(@PathVariable Long id, Model model) {
        AnnouncementEntity announcement = announcementService.findById(id);
        model.addAttribute("announcement", announcement);
        return "announcement/edit";
    }

    @PostMapping("/{id}")
    public String updateAnnouncement(@PathVariable Long id,
                                     @ModelAttribute AnnouncementDTO announcementDTO,
                                     @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        AnnouncementEntity updatedAnnouncement = new AnnouncementEntity();
        updatedAnnouncement.setTitle(announcementDTO.getTitle());
        updatedAnnouncement.setContent(announcementDTO.getContent());

        announcementService.update(id, updatedAnnouncement, imageFile);

        return "redirect:/announcements";
    }

    @PostMapping("/{id}/delete")
    public String deleteAnnouncement(@PathVariable Long id) {
        announcementService.delete(id);
        return "redirect:/announcements";
    }
}
