package com.reservation.web.controller;

import com.reservation.web.dto.ReservationDTO;
import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.service.CourseService;
import com.reservation.web.service.ReservationService;
import com.reservation.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final CourseService courseService;
    private final UserService userService;

    @Autowired
    public ReservationController(ReservationService reservationService,
                                 CourseService courseService,
                                 UserService userService) {
        this.reservationService = reservationService;
        this.courseService = courseService;
        this.userService = userService;
    }

    @GetMapping("/search")
    public String showNonMemberSearchInputForm(Model model) {
        return "reservation/nonMemberSearchInputForm";
    }

    @PostMapping("/search")
    public String searchNonMemberReservationsByPhoneNumber(@RequestParam("name") String name,
                                                           @RequestParam("phoneNumber") String phoneNumber,
                                                           Model model) {
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("searchError", "예약자명을 입력해주세요.");
            return "reservation/nonMemberSearchInputForm";
        }

        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);

        if (normalizedPhoneNumber == null || normalizedPhoneNumber.isBlank()) {
            model.addAttribute("searchError", "전화번호를 입력해주세요.");
            return "reservation/nonMemberSearchInputForm";
        }

        validateKoreanPhoneNumber(normalizedPhoneNumber);

        List<ReservationDTO> reservationDTOs = reservationService.findGuestReservations(name, normalizedPhoneNumber)
                .stream()
                .map(reservationService::convertToGuestLookupDto)
                .collect(Collectors.toList());

        model.addAttribute("reservations", reservationDTOs);
        model.addAttribute("searchedName", name.trim());
        model.addAttribute("searchedPhoneNumber", normalizedPhoneNumber);
        return "reservation/search_non_member";
    }

    @GetMapping("/new/member")
    @PreAuthorize("isAuthenticated()")
    public String showMemberReservationForm(@RequestParam(value = "courseId", required = false) Long courseId,
                                            Model model) {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            return "redirect:/login";
        }

        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setUserId(currentUserId);

        if (courseId != null) {
            reservationDTO.setCourseId(courseId);
        }

        model.addAttribute("reservationDTO", reservationDTO);
        model.addAttribute("courses", courseService.findAllCoursesWithStaff());
        return "reservation/new_member";
    }

    @GetMapping("/new/non-member")
    public String showNonMemberReservationForm(@RequestParam(value = "courseId", required = false) Long courseId,
                                               Model model) {
        ReservationDTO reservationDTO = new ReservationDTO();

        if (courseId != null) {
            reservationDTO.setCourseId(courseId);
        }

        model.addAttribute("reservationDTO", reservationDTO);
        model.addAttribute("courses", courseService.findAllCoursesWithStaff());
        return "reservation/new_non_member";
    }

    @PostMapping("/save")
    public String saveReservation(@ModelAttribute("reservationDTO") ReservationDTO reservationDTO,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (reservationDTO.getCourseId() == null || reservationDTO.getReservationDateTime() == null) {
                throw new IllegalArgumentException("코스와 예약 희망 일시는 필수 항목입니다.");
            }

            String currentUserId = userService.getCurrentUserId();

            if (currentUserId != null && !currentUserId.trim().isEmpty()) {
                reservationDTO.setUserId(currentUserId);
                reservationDTO.setName(null);
                reservationDTO.setPhoneNumber(null);
            } else {
                reservationDTO.setUserId(null);

                if (reservationDTO.getName() == null || reservationDTO.getName().trim().isEmpty()) {
                    throw new IllegalArgumentException("비회원 예약 시 예약자명은 필수 항목입니다.");
                }

                if (reservationDTO.getPhoneNumber() == null || reservationDTO.getPhoneNumber().trim().isEmpty()) {
                    throw new IllegalArgumentException("비회원 예약 시 연락처는 필수 항목입니다.");
                }

                String normalizedPhoneNumber = normalizePhoneNumber(reservationDTO.getPhoneNumber());
                validateKoreanPhoneNumber(normalizedPhoneNumber);
                reservationDTO.setPhoneNumber(normalizedPhoneNumber);
            }

            reservationService.saveReservation(reservationDTO);
            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 등록되었습니다.");

            if (currentUserId != null && !currentUserId.trim().isEmpty()) {
                return "redirect:/reservations";
            }

            return "redirect:/reservations/search";

        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            String currentUserId = userService.getCurrentUserId();
            String redirectPath = (currentUserId != null && !currentUserId.trim().isEmpty())
                    ? "/reservations/new/member"
                    : "/reservations/new/non-member";

            if (reservationDTO.getCourseId() != null) {
                redirectPath += "?courseId=" + reservationDTO.getCourseId();
            }

            return "redirect:" + redirectPath;
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public String viewUserReservations(Model model) {
        String userId = userService.getCurrentUserId();
        if (userId == null || userId.trim().isEmpty()) {
            return "redirect:/login";
        }

        List<ReservationDTO> reservationDTOs = reservationService.findByUserId(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("reservations", reservationDTOs);
        return "reservation/list_user";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewAllReservations(Model model) {
        List<ReservationDTO> reservationDTOs = reservationService.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("reservations", reservationDTOs);
        return "reservation/list_admin";
    }

    @GetMapping("/admin/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditReservationForm(@PathVariable String id,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        Optional<ReservationEntity> reservationEntityOptional = reservationService.findById(id);

        if (reservationEntityOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "유효하지 않은 예약 ID: " + id);
            return "redirect:/reservations/admin";
        }

        ReservationEntity reservationEntity = reservationEntityOptional.get();
        ReservationDTO reservationDTO = convertToDto(reservationEntity);

        model.addAttribute("reservation", reservationDTO);
        model.addAttribute("courses", courseService.findAllCoursesWithStaff());
        return "reservation/edit_admin";
    }

    @PostMapping("/admin/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateReservation(@PathVariable String id,
                                    @ModelAttribute("reservation") ReservationDTO reservationDTO,
                                    RedirectAttributes redirectAttributes) {
        try {
            if (reservationDTO.getId() == null || !reservationDTO.getId().equals(id)) {
                reservationDTO.setId(id);
            }

            reservationService.updateReservation(id, reservationDTO);
            redirectAttributes.addFlashAttribute("successMessage", "예약 ID '" + id + "' 정보가 성공적으로 수정되었습니다.");
            return "redirect:/reservations/admin";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 수정 실패: " + e.getMessage());
            return "redirect:/reservations/admin/" + id + "/edit";
        }
    }

    @PostMapping("/admin/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteReservation(@PathVariable String id,
                                    RedirectAttributes redirectAttributes) {
        try {
            reservationService.deleteReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "예약 ID '" + id + "' 정보가 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 삭제 실패: " + e.getMessage());
        }

        return "redirect:/reservations/admin";
    }

    @PostMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeReservationStatus(@PathVariable String id,
                                          @RequestParam("status") String status,
                                          RedirectAttributes redirectAttributes) {
        try {
            ReservationEntity reservationEntity = reservationService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. ID: " + id));

            ReservationDTO reservationDTO = convertToDto(reservationEntity);
            reservationDTO.setStatus(status);

            reservationService.updateReservation(id, reservationDTO);
            redirectAttributes.addFlashAttribute("successMessage", "예약 ID '" + id + "'의 상태가 '" + status + "'(으)로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "상태 변경 실패: " + e.getMessage());
        }

        return "redirect:/reservations/admin";
    }

    private ReservationDTO convertToDto(ReservationEntity entity) {
        if (entity == null) {
            return null;
        }

        ReservationDTO dto = new ReservationDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());

        if (entity.getCourse() != null) {
            dto.setCourseId(entity.getCourse().getId());
        }

        dto.setReservationDateTime(entity.getReservationDateTime());
        dto.setStatus(entity.getStatus());
        dto.setName(entity.getName());
        dto.setPhoneNumber(entity.getPhoneNumber());

        return dto;
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        return phoneNumber.replaceAll("[^0-9]", "");
    }

    private void validateKoreanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("전화번호를 입력해주세요.");
        }

        String regex = "^(01[016789]\\d{7,8}|02\\d{7,8}|0[3-9]\\d{8,9})$";
        if (!phoneNumber.matches(regex)) {
            throw new IllegalArgumentException("올바른 대한민국 전화번호 형식이 아닙니다.");
        }
    }
}
