package com.reservation.web.controller;

import com.reservation.web.dto.ReservationDTO; // DTO 사용
import com.reservation.web.entity.ReservationEntity; // Entity는 Service/Repository와 주로 사용
import com.reservation.web.service.ReservationService;
import com.reservation.web.service.CourseService;
import com.reservation.web.service.UserService; // 사용자 정보 서비스
import com.reservation.web.entity.CourseEntity; // 폼에서 코스 목록을 위해 Entity를 그대로 사용한다면 필요

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // Optional 사용
import java.util.stream.Collectors; // List 변환 시 필요

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
    @GetMapping("/new/member")
    // @PreAuthorize("isAuthenticated()") // 로그인된 사용자만 접근 가능하도록 권한 추가 고려
    public String showMemberReservationForm(@RequestParam(value = "courseId", required = false) Long courseId, Model model) {
        ReservationDTO reservationDTO = new ReservationDTO();
        if (courseId != null) {
            reservationDTO.setCourseId(courseId); // 선택된 코스 ID를 DTO에 설정
        }

        // 회원 예약 폼에서는 현재 로그인된 사용자 ID를 미리 DTO에 설정해주는 것이 좋습니다.
        // TODO: UserService에서 현재 사용자 ID를 가져오는 로직 구현 필요 (예: SecurityContextHolder 사용)
        // String currentUserId = userService.getCurrentUserId(); // 예시
        // if (currentUserId != null) {
        //     reservationDTO.setUserId(currentUserId);
        // } else {
        // 로그인되지 않은 경우 회원 예약 폼에 접근할 수 없도록 처리 (예: 로그인 페이지 리다이렉트)
        //     return "redirect:/login";
        // }


        model.addAttribute("reservationDTO", reservationDTO);
        // 템플릿에서 CourseEntity의 staff 필드에 접근 가능해야 합니다.
        model.addAttribute("courses", courseService.findAllCoursesWithStaff());
        // TODO: new_member.html 템플릿도 DTO 구조에 맞게 수정해야 합니다.
        return "reservation/new_member";
    }

    /**
     * 비회원 예약 폼
     */
    @GetMapping("/new/non-member")
    public String showNonMemberReservationForm(@RequestParam(value = "courseId", required = false) Long courseId, Model model) {
        ReservationDTO reservationDTO = new ReservationDTO();
        if (courseId != null) {
            reservationDTO.setCourseId(courseId); // 선택된 코스 ID를 DTO에 설정
        }
        model.addAttribute("reservationDTO", reservationDTO);
        model.addAttribute("courses", courseService.findAllCoursesWithStaff());
        // TODO: new_non_member.html 템플릿도 DTO 구조에 맞게 수정해야 합니다.
        return "reservation/new_non-member";
    }

    /**
     * 예약 저장 (회원 및 비회원 공통)
     */
    @PostMapping("/save")
    public String saveReservation(@ModelAttribute ReservationDTO reservationDTO, RedirectAttributes redirectAttributes) {
        try {
            // 1. DTO에 대한 기본적인 유효성 검사 (컨트롤러 또는 서비스에서 수행 가능)
            if (reservationDTO.getCourseId() == null || reservationDTO.getReservationDateTime() == null) {
                throw new IllegalArgumentException("코스와 예약 희망 일시는 필수 항목입니다.");
            }

            // 비회원 예약인 경우 이름과 전화번호 필수 확인
            // 회원 예약인 경우 DTO의 userId는 서비스에서 Authentication 객체를 통해 가져오는 것이 안전합니다.
            // 현재 DTO에 userId가 넘어온다고 가정하고, 비회원만 이름/전화번호 확인
            if (reservationDTO.getUserId() == null || reservationDTO.getUserId().trim().isEmpty()) { // userId가 비어있으면 비회원으로 간주
                if (reservationDTO.getName() == null || reservationDTO.getName().trim().isEmpty() ||
                        reservationDTO.getPhoneNumber() == null || reservationDTO.getPhoneNumber().trim().isEmpty()) {
                    throw new IllegalArgumentException("예약자명과 연락처는 비회원 예약 시 필수 항목입니다.");
                }
            } else {
                // 회원 예약의 경우 DTO에 userId가 넘어왔다면, 서비스에서 Authentication 객체와 비교하여 일치하는지 확인할 수 있습니다.
                // 또는 여기서 Authentication에서 가져온 userId를 DTO에 덮어씌우는 것이 가장 안전합니다.
                // 예: String currentUserId = userService.getCurrentUserId(); // Authentication에서 가져옴
                // if (currentUserId == null || !currentUserId.equals(reservationDTO.getUserId())) {
                //    throw new IllegalStateException("인증된 사용자 정보가 일치하지 않습니다.");
                // }
                // reservationDTO.setUserId(currentUserId); // DTO의 userId를 인증된 사용자로 강제 설정
                // TODO: 실제 UserService 구현에 따라 회원 ID 처리 로직 필요
            }


            // 2. 서비스의 saveReservation 메소드에 DTO 객체를 바로 전달
            // 서비스가 DTO를 Entity로 변환하고 CourseEntity 관계를 설정합니다.
            reservationService.saveReservation(reservationDTO);

            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 등록되었습니다.");

            // 예약 성공 후 리다이렉트 경로 결정
            // 회원 예약이면 회원 예약 목록, 비회원 예약이면 비회원 검색 폼 등으로 리다이렉트
            if (reservationDTO.getUserId() != null && !reservationDTO.getUserId().trim().isEmpty()) {
                return "redirect:/reservations"; // 회원 예약 목록 페이지
            } else {
                return "redirect:/reservations/search"; // 비회원 예약 검색 폼 페이지
            }

        } catch (IllegalStateException | IllegalArgumentException e) {
            // 서비스 로직에서 발생한 오류 메시지를 flash attribute로 전달
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            // 오류 발생 시 이전 폼으로 돌아가도록 리다이렉트
            // courseId와 reservationDateTime을 쿼리 파라미터로 유지하여 폼 상태 복원
            String redirectPath = "/reservations/new/";
            if (reservationDTO.getUserId() != null && !reservationDTO.getUserId().trim().isEmpty()) {
                redirectPath += "member";
            } else {
                redirectPath += "non-member";
            }

            String queryParams = "";
            if (reservationDTO.getCourseId() != null) {
                queryParams += "courseId=" + reservationDTO.getCourseId();
            }
            if (reservationDTO.getReservationDateTime() != null) {
                if (!queryParams.isEmpty()) queryParams += "&";
                queryParams += "reservationDateTime=" + reservationDTO.getReservationDateTime().toString();
            }
            if (!queryParams.isEmpty()) {
                redirectPath += "?" + queryParams;
            }


            return "redirect:" + redirectPath;
        }
    }

    /**
     * 회원 자신의 예약 목록 보기
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')") // 로그인된 USER 역할만 접근 가능
    public String viewUserReservations(Model model) {
        // TODO: UserService에서 현재 사용자 ID를 가져오는 안전한 방법 구현 필요
        String userId = userService.getCurrentUserId(); // 예시: SecurityContextHolder 등 사용
        if (userId == null || userId.trim().isEmpty()) {
            // 로그인되지 않은 경우 또는 userId를 가져오지 못한 경우 처리 (PreAuthorize에 의해 대부분 처리됨)
            return "redirect:/login"; // 또는 오류 페이지
        }
        List<ReservationEntity> reservationEntities = reservationService.findByUserId(userId);
        // Entity 목록을 DTO 목록으로 변환하여 뷰에 전달
        List<ReservationDTO> reservationDTOs = reservationEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("reservations", reservationDTOs); // DTO 목록 전달
        // TODO: list_user.html 템플릿도 DTO 구조에 맞게 수정해야 합니다.
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
        List<ReservationEntity> reservationEntities = reservationService.findByPhoneNumber(phoneNumber);
        // Entity 목록을 DTO 목록으로 변환
        List<ReservationDTO> reservationDTOs = reservationEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("reservations", reservationDTOs); // DTO 목록 전달
        // TODO: list_non_member.html 템플릿도 DTO 구조에 맞게 수정해야 합니다.
        return "reservation/list_non_member";
    }

    /**
     * 관리자: 모든 예약 목록 보기
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 역할만 접근 가능
    public String viewAllReservations(Model model) {
        List<ReservationEntity> reservationEntities = reservationService.findAll();
        // Entity 목록을 DTO 목록으로 변환
        List<ReservationDTO> reservationDTOs = reservationEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("reservations", reservationDTOs); // DTO 목록 전달
        // TODO: list_admin.html 템플릿도 DTO 구조에 맞게 수정해야 합니다.
        return "reservation/list_admin";
    }

    // 관리자: 예약 상세 보기 및 수정 폼
    @GetMapping("/admin/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 역할만 접근 가능
    public String showEditReservationForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) { // ID 타입을 String으로 변경
        Optional<ReservationEntity> reservationEntityOptional = reservationService.findById(id); // 서비스 호출 시 String ID 전달

        if (!reservationEntityOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "유효하지 않은 예약 ID: " + id);
            return "redirect:/reservations/admin"; // 관리자 목록 페이지로 리다이렉트
        }

        ReservationEntity reservationEntity = reservationEntityOptional.get();

        // 폼 바인딩을 위해 ReservationDTO로 변환하여 모델에 추가
        model.addAttribute("reservationDTO", convertToDto(reservationEntity));
        model.addAttribute("courses", courseService.findAllCoursesWithStaff()); // CourseEntity 목록
        // TODO: edit_admin.html 템플릿도 DTO와 CourseEntity 구조에 맞게 수정해야 합니다.
        return "reservation/edit_admin";
    }

    /**
     * 관리자: 예약 수정
     */
    @PostMapping("/admin/{id}/update")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 역할만 접근 가능
    public String updateReservation(@PathVariable String id, @ModelAttribute ReservationDTO reservationDTO, RedirectAttributes redirectAttributes) { // ID 타입을 String으로 변경
        try {
            // DTO에 ID 설정 (PathVariable로 받은 ID를 DTO에 넣어 서비스로 전달)
            reservationDTO.setId(id); // DTO의 ID도 String

            // 서비스에서 DTO와 ID를 받아 업데이트 로직 처리
            reservationService.updateReservation(id, reservationDTO); // 서비스 호출 시 String ID 전달
            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 수정되었습니다.");
            return "redirect:/reservations/admin";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // 오류 발생 시 수정 폼으로 돌아가도록 리다이렉트
            return "redirect:/reservations/admin/" + id + "/edit";
        }
    }

    /**
     * 관리자: 예약 삭제
     */
    @PostMapping("/admin/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 역할만 접근 가능
    public String deleteReservation(@PathVariable String id, RedirectAttributes redirectAttributes) { // ID 타입을 String으로 변경
        try {
            reservationService.deleteReservation(id); // 서비스 호출 시 String ID 전달
            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/reservations/admin";
    }

    /**
     * 관리자: 예약 상태 변경 (확인, 취소 등)
     */
    @PostMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 역할만 접근 가능
    public String changeReservationStatus(@PathVariable String id, @RequestParam("status") String status, RedirectAttributes redirectAttributes) { // ID 타입을 String으로 변경
        try {
            // 서비스에서 해당 예약을 찾음
            ReservationEntity reservationEntity = reservationService.findById(id) // 서비스 호출 시 String ID 전달
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

            // Entity의 상태만 변경하고 업데이트 서비스 호출
            reservationEntity.setStatus(status);
            // 상태 변경만 하는 경우 DTO 변환 없이 Entity를 직접 넘겨도 될 수 있으나,
            // updateReservation 서비스가 DTO를 받도록 설계되었으므로 DTO로 변환 후 상태만 변경
            ReservationDTO reservationDTO = convertToDto(reservationEntity);
            reservationDTO.setStatus(status); // 상태만 다시 설정 (convertToDto에서 복사된 기존 상태 위에 덮어씌움)

            reservationService.updateReservation(id, reservationDTO); // 서비스 호출 시 String ID와 DTO 전달

            redirectAttributes.addFlashAttribute("successMessage", "예약 상태가 변경되었습니다.");
            return "redirect:/reservations/admin"; // 관리자 목록으로 리다이렉트
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // 오류 발생 시 관리자 목록으로 리다이렉트 또는 상세/수정 폼으로 리다이렉트
            return "redirect:/reservations/admin"; // 또는 "/reservations/admin/" + id + "/edit";
        }
    }

    // ReservationEntity를 ReservationDTO로 변환하는 헬퍼 메소드
    // String ID 복사 로직 유지
    private ReservationDTO convertToDto(ReservationEntity entity) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(entity.getId()); // DTO에 String id 필드 있으므로 복사
        dto.setUserId(entity.getUserId());
        // Entity에 CourseEntity 객체가 있다면 ID를 DTO에 설정 (CourseEntity의 ID는 Long)
        if (entity.getCourse() != null) {
            dto.setCourseId(entity.getCourse().getId());
        }
        dto.setReservationDateTime(entity.getReservationDateTime());
        dto.setStatus(entity.getStatus()); // DTO에 status 필드 있으므로 복사
        dto.setName(entity.getName());
        dto.setPhoneNumber(entity.getPhoneNumber());
        return dto;
    }

    // 참고: ReservationDTO -> ReservationEntity 변환은 서비스에서 처리하는 것이 일반적입니다.
}