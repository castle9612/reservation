package com.reservation.web.controller;

import com.reservation.web.dto.StaffDTO;
import com.reservation.web.service.StaffService;
import jakarta.validation.Valid; // Spring Boot 3.x
// import javax.validation.Valid; // Spring Boot 2.x
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
@PreAuthorize("hasRole('ADMIN')") // 클래스 레벨에서 관리자 권한 적용
public class StaffController {

    private final StaffService staffService;

    // 스태프 목록 조회
    @GetMapping
    public String listStaff(Model model) {
        List<StaffDTO> staffList = staffService.findAllStaff();
        model.addAttribute("staffList", staffList);
        return "staff/staffList";
    }

    // 새 스태프 등록 폼
    @GetMapping("/new")
    public String newStaffForm(Model model) {
        if (!model.containsAttribute("staffDTO")) { // 리다이렉트로 전달된 DTO가 없을 경우 새 DTO 생성
            model.addAttribute("staffDTO", new StaffDTO());
        }
        return "staff/staffForm";
    }

    // 새 스태프 등록 처리
    // StaffController.java
    @PostMapping // 또는 명시적 경로: @PostMapping("/staff")
    public String saveStaff(@Valid @ModelAttribute("staffDTO") StaffDTO staffDTO,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        System.out.println("!!!!!!!! StaffController - saveStaff METHOD CALLED !!!!!!!!"); // 이 로그가 찍히는지 확인
        System.out.println("Received StaffDTO for saving: ID=" + staffDTO.getId() + ", Name=" + staffDTO.getName());

        if (bindingResult.hasErrors()) {
            System.out.println("Validation errors found: " + bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.staffDTO", bindingResult);
            redirectAttributes.addFlashAttribute("staffDTO", staffDTO);
            // 새 스태프 등록 실패 시 /staff/new로, 수정 실패 시 /staff/edit/{id}로 가는 것이 더 적절할 수 있음
            // 현재는 새 스태프 등록 시에만 이 POST 메소드가 호출된다고 가정하고 /staff/new로 리다이렉트
            return "redirect:/staff/new";
        }
        try {
            System.out.println("Attempting to save staff via service...");
            staffService.saveStaff(staffDTO); // 이 호출이 실제로 일어나는지 확인
            System.out.println("Staff saved successfully via service.");
            redirectAttributes.addFlashAttribute("successMessage", "스태프가 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            System.err.println("Error saving staff: " + e.getMessage());
            e.printStackTrace(); // 전체 스택 트레이스 출력
            redirectAttributes.addFlashAttribute("errorMessage", "스태프 등록 중 오류가 발생했습니다: " + e.getMessage());
            redirectAttributes.addFlashAttribute("staffDTO", staffDTO);
            return "redirect:/staff/new";
        }
        System.out.println("Redirecting to /staff after saving.");
        return "redirect:/staff";
    }

    // 스태프 수정 폼
    @GetMapping("/edit/{staffId}")
    public String editStaffForm(@PathVariable Long staffId, Model model, RedirectAttributes redirectAttributes) {
        if (!model.containsAttribute("staffDTO")) { // 리다이렉트로 전달된 DTO가 없을 경우 DB에서 조회
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

    // 스태프 수정 처리
    @PostMapping("/edit/{staffId}")
    public String updateStaff(@PathVariable Long staffId,
                              @Valid @ModelAttribute("staffDTO") StaffDTO staffDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // 유효성 검사 실패 시, 입력값 유지하며 폼으로 다시 리다이렉트
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.staffDTO", bindingResult);
            redirectAttributes.addFlashAttribute("staffDTO", staffDTO); // staffDTO에 staffId가 포함되어 있어야 함
            return "redirect:/staff/edit/" + staffId; // GET 요청으로 리다이렉트
        }
        try {
            staffDTO.setId(staffId); // 경로 변수의 ID를 DTO에 설정
            staffService.saveStaff(staffDTO); // ID가 있으므로 수정
            redirectAttributes.addFlashAttribute("successMessage", "스태프 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "스태프 수정 중 오류가 발생했습니다: " + e.getMessage());
            redirectAttributes.addFlashAttribute("staffDTO", staffDTO);
            return "redirect:/staff/edit/" + staffId;
        }
        return "redirect:/staff";
    }

    // 스태프 삭제 처리
    @PostMapping("/delete/{staffId}")
    public String deleteStaff(@PathVariable Long staffId, RedirectAttributes redirectAttributes) {
        try {
            staffService.deleteStaff(staffId);
            redirectAttributes.addFlashAttribute("successMessage", "스태프가 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "삭제할 스태프를 찾을 수 없습니다.");
        } catch (Exception e) { // 예를 들어, 다른 곳에서 참조하고 있어 삭제할 수 없는 경우 등
            redirectAttributes.addFlashAttribute("errorMessage", "스태프 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/staff";
    }
}