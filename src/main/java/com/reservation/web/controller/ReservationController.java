package com.reservation.web.controller;

import com.reservation.web.dto.ReservationDTO;
import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.service.ReservationService;
import com.reservation.web.service.CourseService;
import com.reservation.web.service.UserService; // 사용자 정보를 가져오기 위한 서비스 (필요 시 구현)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final CourseService courseService;
    private final UserService userService; // 사용자 정보를 가져오기 위한 서비스

    @Autowired
    public ReservationController(ReservationService reservationService, CourseService courseService, UserService userService) {
        this.reservationService = reservationService;
        this.courseService = courseService;
        this.userService = userService;
    }

    /**
     * 회원 예약 폼
     */
    // 회원 예약 폼
    @GetMapping("/new/member")
    public String showMemberReservationForm(@RequestParam(value = "courseId", required = false) Long courseId, Model model) { // 코스 ID를 받을 수 있도록 변경
        ReservationDTO reservationDTO = new ReservationDTO();
        if (courseId != null) {
            reservationDTO.setCourseId(courseId); // 선택된 코스 ID를 DTO에 설정
        }
        model.addAttribute("reservationDTO", reservationDTO);
        // findAllCoursesWithStaff()를 사용하여 Staff 정보도 포함된 CourseEntity 목록을 가져옴
        model.addAttribute("courses", courseService.findAllCoursesWithStaff());
        return "reservation/new_member";
    }

    /**
     * 비회원 예약 폼
     */
    @GetMapping("/new/non-member")
    public String showNonMemberReservationForm(@RequestParam(value = "courseId", required = false) Long courseId, Model model) { // 코스 ID를 받을 수 있도록 변경
        ReservationDTO reservationDTO = new ReservationDTO();
        if (courseId != null) {
            reservationDTO.setCourseId(courseId); // 선택된 코스 ID를 DTO에 설정
        }
        model.addAttribute("reservationDTO", reservationDTO);
        model.addAttribute("courses", courseService.findAllCoursesWithStaff());
        return "reservation/new_non-member";
    }

    /**
     * 예약 저장
     */
    @PostMapping("/save")
    public String saveReservation(@ModelAttribute ReservationDTO reservationDTO, RedirectAttributes redirectAttributes) {
        try {
            ReservationEntity reservation = new ReservationEntity();
            reservation.setCourseId(reservationDTO.getCourseId());
            reservation.setReservationDateTime(reservationDTO.getReservationDateTime());

            // 회원 예약인 경우
            if (reservationDTO.getUserId() != null) {
                reservation.setUserId(reservationDTO.getUserId());
            } else { // 비회원 예약인 경우
                reservation.setName(reservationDTO.getName());
                reservation.setPhoneNumber(reservationDTO.getPhoneNumber());
            }

            reservationService.saveReservation(reservation);
            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 등록되었습니다.");
            return "redirect:/reservations";
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reservations/new/" + (reservationDTO.getUserId() != null ? "member" : "non-member");
        }
    }

    /**
     * 회원 자신의 예약 목록 보기
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public String viewUserReservations(Model model) {
        String userId = userService.getCurrentUserId(); // 현재 로그인한 사용자의 ID 가져오기 (UserService 구현 필요)
        List<ReservationEntity> reservations = reservationService.findByUserId(userId);
        model.addAttribute("reservations", reservations);
        return "reservation/list_user";
    }

    /**
     * 비회원 예약 검색 폼
     */
    @GetMapping("/search")
    public String showNonMemberSearchForm() {
        return "reservation/search_non_member";
    }

    /**
     * 비회원 예약 검색 결과
     */
    @PostMapping("/search")
    public String searchNonMemberReservations(@RequestParam("phoneNumber") String phoneNumber, Model model) {
        List<ReservationEntity> reservations = reservationService.findByPhoneNumber(phoneNumber);
        model.addAttribute("reservations", reservations);
        return "reservation/list_non_member";
    }

    /**
     * 관리자: 모든 예약 목록 보기
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewAllReservations(Model model) {
        List<ReservationEntity> reservations = reservationService.findAll();
        model.addAttribute("reservations", reservations);
        return "reservation/list_admin";
    }

    // 관리자: 예약 상세 보기 및 수정 폼
    @GetMapping("/admin/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditReservationForm(@PathVariable String id, Model model) {
        ReservationEntity reservation = reservationService.findById(id);
        if (reservation == null) {
            // ... 오류 처리
        }
        model.addAttribute("reservation", reservation);
        model.addAttribute("courses", courseService.findAllCoursesWithStaff()); // 여기도 변경
        return "reservation/edit_admin";
    }

    /**
     * 관리자: 예약 수정
     */
    @PostMapping("/admin/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateReservation(@PathVariable String id, @ModelAttribute ReservationEntity reservation, RedirectAttributes redirectAttributes) {
        try {
            reservationService.updateReservation(id, reservation);
            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 수정되었습니다.");
            return "redirect:/reservations/admin";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reservations/admin/" + id + "/edit";
        }
    }

    /**
     * 관리자: 예약 삭제
     */
    @PostMapping("/admin/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteReservation(@PathVariable String id, RedirectAttributes redirectAttributes) {
        reservationService.deleteReservation(id);
        redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 삭제되었습니다.");
        return "redirect:/reservations/admin";
    }

    /**
     * 관리자: 예약 상태 변경 (확인, 취소 등)
     */
    @PostMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeReservationStatus(@PathVariable String id, @RequestParam("status") String status, RedirectAttributes redirectAttributes) {
        try {
            ReservationEntity reservation = reservationService.findById(id);
            if (reservation == null) {
                throw new IllegalArgumentException("존재하지 않는 예약입니다.");
            }
            reservation.setStatus(status);
            reservationService.updateReservation(id, reservation);
            redirectAttributes.addFlashAttribute("successMessage", "예약 상태가 변경되었습니다.");
            return "redirect:/reservations/admin";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reservations/admin/" + id + "/edit";
        }
    }
}
