package com.reservation.web.controller;

import com.reservation.web.service.AnnouncementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("/announcements")
    public String getAnnouncements(Model model) {
        model.addAttribute("announcements", announcementService.findAll());
        return "announcement";  // View 파일 경로
    }
}
