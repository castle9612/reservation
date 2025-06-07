package com.reservation.web.controller;

import com.reservation.web.dto.ReservationDTO;
import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.service.ReservationService;
import com.reservation.web.service.CourseService;
import com.reservation.web.service.UserService;
// import com.reservation.web.entity.CourseEntity; // CourseService에서 CourseEntity를 반환하므로 직접 사용 가능

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
    public ReservationController(ReservationService reservationService, CourseService courseService, UserService userService) {
        this.reservationService = reservationService;
        this.courseService = courseService;
        this.userService = userService;
    }

    @GetMapping("/search")
    public String showNonMemberSearchInputForm(Model model) { // Model 추가 (혹시 폼에 전달할 초기 데이터가 있다면)
        // 여기에 phoneNumber 필드만 있는 간단한 DTO를 모델에 추가하여 th:object를 사용할 수도 있습니다.
        // 예: model.addAttribute("searchRequest", new NonMemberSearchRequestDTO());
        return "reservation/nonMemberSearchInputForm"; // 새로 만든 입력 폼 페이지 반환
    }

    /**
     * 비회원 예약을 전화번호로 검색하고 결과를 보여줍니다. (POST 요청)
     * 입력 폼에서 "조회하기" 버튼 클릭 시 이 메소드가 호출됩니다.
     */
    @PostMapping("/search")
    public String searchNonMemberReservationsByPhoneNumber(@RequestParam("phoneNumber") String phoneNumber, Model model) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            model.addAttribute("searchError", "전화번호를 입력해주세요.");
            return "reservation/nonMemberSearchInputForm"; // 에러 메시지와 함께 다시 입력 폼으로
        }

        List<ReservationDTO> reservationDTOs = reservationService.findByPhoneNumber(phoneNumber)
                .stream()
                .map(this::convertToDto) // Entity -> DTO 변환
                .collect(Collectors.toList());

        model.addAttribute("reservations", reservationDTOs);
        model.addAttribute("searchedPhoneNumber", phoneNumber); // 검색한 번호도 전달 (결과 페이지에서 표시용)
        return "reservation/search_non_member"; // 기존 결과 페이지 (search_non_member.html) 반환
    }

    // ... (showMemberReservationForm, showNonMemberReservationForm, saveReservation 메소드는 이전과 동일하게 유지)
    /**
     * 회원 예약 폼
     */
    @GetMapping("/new/member")
    public String showMemberReservationForm(@RequestParam(value = "courseId", required = false) Long courseId, Model model) {
        ReservationDTO reservationDTO = new ReservationDTO();
        if (courseId != null) {
            reservationDTO.setCourseId(courseId);
        }
        // 예시: 현재 사용자 ID 설정 (실제 구현 필요)
        // String currentUserId = userService.getCurrentUserId();
        // if (currentUserId != null) {
        //     reservationDTO.setUserId(currentUserId);
        // } else {
        //     return "redirect:/login"; // 로그인되지 않은 경우
        // }
        model.addAttribute("reservationDTO", reservationDTO); // 폼 객체 이름은 DTO로 유지
        model.addAttribute("courses", courseService.findAllCoursesWithStaff());
        return "reservation/new_member";
    }

    /**
     * 비회원 예약 폼
     */
    @GetMapping("/new/non-member")
    public String showNonMemberReservationForm(@RequestParam(value = "courseId", required = false) Long courseId, Model model) {
        ReservationDTO reservationDTO = new ReservationDTO();
        if (courseId != null) {
            reservationDTO.setCourseId(courseId);
        }
        model.addAttribute("reservationDTO", reservationDTO); // 폼 객체 이름은 DTO로 유지
        model.addAttribute("courses", courseService.findAllCoursesWithStaff());
        return "reservation/new_non_member";
    }

    /**
     * 예약 저장 (회원 및 비회원 공통)
     */
    @PostMapping("/save")
    public String saveReservation(@ModelAttribute("reservationDTO") ReservationDTO reservationDTO, RedirectAttributes redirectAttributes) { // @ModelAttribute에 이름 명시
        try {
            if (reservationDTO.getCourseId() == null || reservationDTO.getReservationDateTime() == null) {
                throw new IllegalArgumentException("코스와 예약 희망 일시는 필수 항목입니다.");
            }

            if (reservationDTO.getUserId() == null || reservationDTO.getUserId().trim().isEmpty()) {
                if (reservationDTO.getName() == null || reservationDTO.getName().trim().isEmpty() ||
                        reservationDTO.getPhoneNumber() == null || reservationDTO.getPhoneNumber().trim().isEmpty()) {
                    throw new IllegalArgumentException("예약자명과 연락처는 비회원 예약 시 필수 항목입니다.");
                }
            } else {
                // 회원 예약 시 userId 처리 (예: 현재 인증된 사용자와 비교)
                // String currentUserId = userService.getCurrentUserId();
                // if (currentUserId == null || !currentUserId.equals(reservationDTO.getUserId())) {
                //    throw new IllegalStateException("인증된 사용자 정보가 일치하지 않습니다.");
                // }
            }

            reservationService.saveReservation(reservationDTO);
            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 등록되었습니다.");

            if (reservationDTO.getUserId() != null && !reservationDTO.getUserId().trim().isEmpty()) {
                return "redirect:/reservations";
            } else {
                return "redirect:/reservations/search";
            }

        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            String redirectPath = "/reservations/new/";
            redirectPath += (reservationDTO.getUserId() != null && !reservationDTO.getUserId().trim().isEmpty()) ? "member" : "non-member";

            // 폼 데이터 유지를 위해 DTO 전체를 FlashAttribute로 전달할 수도 있습니다.
            // redirectAttributes.addFlashAttribute("reservationDTO", reservationDTO);
            // 또는 필요한 필드만 쿼리 파라미터로 전달
            String queryParams = "";
            if (reservationDTO.getCourseId() != null) {
                queryParams += "courseId=" + reservationDTO.getCourseId();
            }
            // reservationDateTime은 문자열로 변환 시 포맷 주의
            // if (reservationDTO.getReservationDateTime() != null) {
            //     if (!queryParams.isEmpty()) queryParams += "&";
            //     queryParams += "reservationDateTime=" + reservationDTO.getReservationDateTime().toString();
            // }
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
    @PreAuthorize("hasRole('USER')")
    public String viewUserReservations(Model model) {
        String userId = userService.getCurrentUserId();
        if (userId == null || userId.trim().isEmpty()) {
            return "redirect:/login";
        }
        List<ReservationDTO> reservationDTOs = reservationService.findByUserId(userId)
                .stream()
                .map(this::convertToDto) // Entity -> DTO 변환
                .collect(Collectors.toList());
        model.addAttribute("reservations", reservationDTOs);
        return "reservation/list_user";
    }

    /**
     * 비회원 예약 검색 폼
     */
//    @GetMapping("/search")
//    public String showNonMemberSearchForm() {
//        return "reservation/search_non_member";
//    }

    /**
     * 비회원 예약 검색 결과
     */
//    @PostMapping("/search")
//    public String searchNonMemberReservations(@RequestParam("phoneNumber") String phoneNumber, Model model) {
//        List<ReservationDTO> reservationDTOs = reservationService.findByPhoneNumber(phoneNumber)
//                .stream()
//                .map(this::convertToDto) // Entity -> DTO 변환
//                .collect(Collectors.toList());
//        model.addAttribute("reservations", reservationDTOs);
//        return "reservation/list_non_member";
//    }

    /**
     * 관리자: 모든 예약 목록 보기
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewAllReservations(Model model) {
        List<ReservationDTO> reservationDTOs = reservationService.findAll()
                .stream()
                .map(this::convertToDto) // Entity -> DTO 변환
                .collect(Collectors.toList());
        model.addAttribute("reservations", reservationDTOs); // DTO 목록 전달
        return "reservation/list_admin";
    }

    /**
     * 관리자: 예약 상세 보기 및 수정 폼
     */
    @GetMapping("/admin/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditReservationForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ReservationEntity> reservationEntityOptional = reservationService.findById(id);

        if (!reservationEntityOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "유효하지 않은 예약 ID: " + id);
            return "redirect:/reservations/admin";
        }

        ReservationEntity reservationEntity = reservationEntityOptional.get();
        ReservationDTO reservationDTO = convertToDto(reservationEntity); // Entity -> DTO 변환

        // 템플릿에서 th:object="${reservation}"을 사용하므로 모델 객체 이름을 "reservation"으로 변경
        model.addAttribute("reservation", reservationDTO); // "reservationDTO" 대신 "reservation" 사용
        model.addAttribute("courses", courseService.findAllCoursesWithStaff());
        return "reservation/edit_admin";
    }

    /**
     * 관리자: 예약 수정
     */
    @PostMapping("/admin/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    // 템플릿에서 th:object="${reservation}"으로 받았으므로 @ModelAttribute("reservation")으로 받음
    public String updateReservation(@PathVariable String id, @ModelAttribute("reservation") ReservationDTO reservationDTO, RedirectAttributes redirectAttributes) {
        try {
            // PathVariable의 ID와 DTO의 ID가 일치하는지 확인하거나, DTO의 ID를 PathVariable로 설정
            if (reservationDTO.getId() == null || !reservationDTO.getId().equals(id)) {
                reservationDTO.setId(id); // DTO의 ID를 PathVariable ID로 강제 설정
            }

            reservationService.updateReservation(id, reservationDTO);
            redirectAttributes.addFlashAttribute("successMessage", "예약 ID '" + id + "' 정보가 성공적으로 수정되었습니다.");
            return "redirect:/reservations/admin";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 수정 실패: " + e.getMessage());
            // 오류 발생 시 수정 폼으로 돌아가도록 리다이렉트
            return "redirect:/reservations/admin/" + id + "/edit";
        }
    }

    /**
     * 관리자: 예약 삭제
     */
    @PostMapping("/admin/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteReservation(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.deleteReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "예약 ID '" + id + "' 정보가 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 삭제 실패: " + e.getMessage());
        }
        return "redirect:/reservations/admin";
    }

    /**
     * 관리자: 예약 상태 변경 (확인, 취소 등)
     */
    @PostMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeReservationStatus(@PathVariable String id, @RequestParam("status") String status, RedirectAttributes redirectAttributes) {
        try {
            // 서비스에서 해당 예약을 찾고 상태만 변경 후 업데이트
            // 기존 ReservationService의 updateReservation을 활용하려면 DTO가 필요
            ReservationEntity reservationEntity = reservationService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. ID: " + id));

            ReservationDTO reservationDTO = convertToDto(reservationEntity);
            reservationDTO.setStatus(status); // 새로운 상태 설정

            reservationService.updateReservation(id, reservationDTO); // ID와 함께 업데이트된 DTO 전달

            redirectAttributes.addFlashAttribute("successMessage", "예약 ID '" + id + "'의 상태가 '" + status + "'(으)로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "상태 변경 실패: " + e.getMessage());
        }
        // 성공/실패 모두 관리자 목록으로 리다이렉트 또는 상세 페이지로
        return "redirect:/reservations/admin"; // 또는 "redirect:/reservations/admin/" + id + "/edit";
    }

    // ReservationEntity를 ReservationDTO로 변환하는 헬퍼 메소드
    private ReservationDTO convertToDto(ReservationEntity entity) {
        if (entity == null) {
            return null; // 또는 빈 DTO 반환 new ReservationDTO();
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
}