package com.reservation.web.controller;

import com.reservation.web.dto.StaffDTO;
import com.reservation.web.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    public String listStaff(Model model) {
        List<StaffDTO> staffList = staffService.findAllStaff();
        model.addAttribute("staffList", staffList);
        return "staff/staffList";
    }

    @GetMapping("/{staffId}")
    public String detailStaff(@PathVariable Long staffId, Model model, RedirectAttributes redirectAttributes) {
        try {
            StaffDTO staffDTO = staffService.findById(staffId);
            model.addAttribute("staff", staffDTO);
            return "staff/staffDetail";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "해당 스태프를 찾을 수 없습니다.");
            return "redirect:/staff";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newStaffForm(Model model) {
        if (!model.containsAttribute("staffDTO")) {
            model.addAttribute("staffDTO", new StaffDTO());
        }
        return "staff/staffForm";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String saveStaff(@Valid @ModelAttribute("staffDTO") StaffDTO staffDTO,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.staffDTO", bindingResult);
            redirectAttributes.addFlashAttribute("staffDTO", staffDTO);
            return "redirect:/staff/new";
        }

        try {
            staffService.saveStaff(staffDTO);
            redirectAttributes.addFlashAttribute("successMessage", "스태프가 성공적으로 등록되었습니다.");
            return "redirect:/staff";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스태프 등록 중 오류가 발생했습니다: " + e.getMessage());
            redirectAttributes.addFlashAttribute("staffDTO", staffDTO);
            return "redirect:/staff/new";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{staffId}")
    public String editStaffForm(@PathVariable Long staffId, Model model, RedirectAttributes redirectAttributes) {
        if (!model.containsAttribute("staffDTO")) {
            try {
                StaffDTO staffDTO = staffService.findById(staffId);
                model.addAttribute("staffDTO", staffDTO);
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "해당 스태프를 찾을 수 없습니다.");
                return "redirect:/staff";
            }
        }
        return "staff/staffForm";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/edit/{staffId}")
    public String updateStaff(@PathVariable Long staffId,
                              @Valid @ModelAttribute("staffDTO") StaffDTO staffDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.staffDTO", bindingResult);
            redirectAttributes.addFlashAttribute("staffDTO", staffDTO);
            return "redirect:/staff/edit/" + staffId;
        }

        try {
            staffDTO.setId(staffId);
            staffService.saveStaff(staffDTO);
            redirectAttributes.addFlashAttribute("successMessage", "스태프 정보가 성공적으로 수정되었습니다.");
            return "redirect:/staff";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스태프 수정 중 오류가 발생했습니다: " + e.getMessage());
            redirectAttributes.addFlashAttribute("staffDTO", staffDTO);
            return "redirect:/staff/edit/" + staffId;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{staffId}")
    public String deleteStaff(@PathVariable Long staffId, RedirectAttributes redirectAttributes) {
        try {
            staffService.deleteStaff(staffId);
            redirectAttributes.addFlashAttribute("successMessage", "스태프가 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "삭제할 스태프를 찾을 수 없습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스태프 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/staff";
    }
}