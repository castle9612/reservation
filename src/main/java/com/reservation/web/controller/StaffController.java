package com.reservation.web.controller;

import com.reservation.web.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/staff")
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    public String getStaffList(Model model) {
        model.addAttribute("staffList", staffService.findAll());
        return "staff/staffList";
    }

    @GetMapping("/{id}")
    public String getStaffDetail(@PathVariable Long id, Model model) {
        model.addAttribute("staff", staffService.findById(id));
        return "staff/staffDetail";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("staff", new Object());
        return "staff/staffForm";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String createStaff(/* DTO dto */) {
        // 저장 처리
        return "redirect:/staff";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("staff", staffService.findById(id));
        return "staff/staffForm";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Long id) {
        staffService.deleteById(id);
        return "redirect:/staff";
    }
}