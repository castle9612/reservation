package com.reservation.web.controller.api;

import com.reservation.web.dto.AnnouncementDTO;
import com.reservation.web.dto.CourseDTO;
import com.reservation.web.dto.ReservationDTO;
import com.reservation.web.dto.StaffDTO;
import com.reservation.web.entity.AnnouncementEntity;
import com.reservation.web.entity.CouponEntity;
import com.reservation.web.entity.CourseEntity;
import com.reservation.web.entity.MileageSettingEntity;
import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.entity.StaffEntity;
import com.reservation.web.entity.UserEntity;
import com.reservation.web.repository.ReservationRepository;
import com.reservation.web.repository.UserRepository;
import com.reservation.web.service.AnnouncementService;
import com.reservation.web.service.CouponService;
import com.reservation.web.service.CourseService;
import com.reservation.web.service.MileageService;
import com.reservation.web.service.ReservationService;
import com.reservation.web.service.StaffService;
import org.springframework.http.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class ApiAdminController {

    private static final List<String> RESERVATION_STATUSES = List.of(
            "PENDING",
            "CONFIRMED",
            "COMPLETED",
            "CANCELLED",
            "CANCELLED_USER",
            "CANCELLED_ADMIN",
            "NO_SHOW"
    );

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final CourseService courseService;
    private final StaffService staffService;
    private final AnnouncementService announcementService;
    private final ReservationService reservationService;
    private final CouponService couponService;
    private final MileageService mileageService;

    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> users() {
        List<Map<String, Object>> data = userRepository.findAll()
                .stream()
                .map(this::toUserMap)
                .toList();
        return ApiResponse.ok(data);
    }

    @GetMapping("/reservations")
    public ApiResponse<List<Map<String, Object>>> reservations() {
        List<ReservationEntity> reservations = reservationRepository.findAllWithCourse();
        Map<String, String> memberPhoneNumbers = userRepository.findAllById(
                        reservations.stream()
                                .map(ReservationEntity::getUserId)
                                .filter(userId -> userId != null && !userId.isBlank())
                                .distinct()
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(UserEntity::getUserId, UserEntity::getPhoneNumber));

        List<Map<String, Object>> data = reservations.stream()
                .map(reservation -> toReservationMap(reservation, memberPhoneNumbers.get(reservation.getUserId())))
                .toList();
        return ApiResponse.ok(data);
    }

    @PostMapping("/courses")
    public ApiResponse<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        CourseEntity saved = courseService.saveCourse(courseDTO);
        return ApiResponse.ok(courseService.convertEntityToDTO(saved), "코스가 등록되었습니다.");
    }

    @PutMapping("/courses/{courseId}")
    public ApiResponse<CourseDTO> updateCourse(@PathVariable Long courseId, @RequestBody CourseDTO courseDTO) {
        CourseEntity updated = courseService.updateCourse(courseId, courseDTO);
        return ApiResponse.ok(courseService.convertEntityToDTO(updated), "코스가 수정되었습니다.");
    }

    @PutMapping("/courses/{courseId}/move")
    public ApiResponse<Void> moveCourse(@PathVariable Long courseId, @RequestBody Map<String, Object> payload) {
        courseService.moveCourse(courseId, stringValue(payload.get("direction"), "up"));
        return ApiResponse.ok(null, "프로그램 순서가 변경되었습니다.");
    }

    @DeleteMapping("/courses/{courseId}")
    public ApiResponse<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ApiResponse.ok(null, "코스가 삭제되었습니다.");
    }

    @PostMapping(value = "/staff", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> createStaff(@RequestPart("staff") StaffDTO staffDTO,
                                                        @RequestPart(name = "profileImage", required = false) MultipartFile profileImage) throws IOException {
        StaffEntity saved = staffService.saveStaff(staffDTO, profileImage);
        return ApiResponse.ok(toStaffMap(saved), "스태프가 등록되었습니다.");
    }

    @PutMapping(value = "/staff/{staffId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> updateStaff(@PathVariable Long staffId,
                                                        @RequestPart("staff") StaffDTO staffDTO,
                                                        @RequestPart(name = "profileImage", required = false) MultipartFile profileImage) throws IOException {
        staffDTO.setId(staffId);
        StaffEntity saved = staffService.saveStaff(staffDTO, profileImage);
        return ApiResponse.ok(toStaffMap(saved), "스태프가 수정되었습니다.");
    }

    @DeleteMapping("/staff/{staffId}")
    public ApiResponse<Void> deleteStaff(@PathVariable Long staffId) {
        staffService.deleteStaff(staffId);
        return ApiResponse.ok(null, "스태프가 삭제되었습니다.");
    }

    @PostMapping(value = "/announcements", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AnnouncementDTO> createAnnouncement(@RequestPart("announcement") AnnouncementDTO announcementDTO,
                                                           @RequestPart(name = "newAttachmentFiles", required = false) MultipartFile[] newAttachmentFiles) throws IOException {
        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setTitle(announcementDTO.getTitle());
        announcement.setContent(announcementDTO.getContent());
        AnnouncementEntity saved = announcementService.save(announcement, newAttachmentFiles);
        return ApiResponse.ok(toAnnouncementDto(saved), "공지사항이 등록되었습니다.");
    }

    @PutMapping(value = "/announcements/{announcementId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AnnouncementDTO> updateAnnouncement(@PathVariable Long announcementId,
                                                           @RequestPart("announcement") AnnouncementDTO announcementDTO,
                                                           @RequestPart(name = "newAttachmentFiles", required = false) MultipartFile[] newAttachmentFiles,
                                                           @RequestParam(name = "deletedAttachmentPaths", required = false) List<String> deletedAttachmentPaths) throws IOException {
        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setTitle(announcementDTO.getTitle());
        announcement.setContent(announcementDTO.getContent());
        AnnouncementEntity saved = announcementService.update(announcementId, announcement, newAttachmentFiles, deletedAttachmentPaths);
        return ApiResponse.ok(toAnnouncementDto(saved), "공지사항이 수정되었습니다.");
    }

    @DeleteMapping("/announcements/{announcementId}")
    public ApiResponse<Void> deleteAnnouncement(@PathVariable Long announcementId) {
        announcementService.delete(announcementId);
        return ApiResponse.ok(null, "공지사항이 삭제되었습니다.");
    }

    @PostMapping(value = "/announcements/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, String>> uploadAnnouncementImage(@RequestPart("image") MultipartFile image) throws IOException {
        String url = announcementService.storeSummernoteImage(image);
        return ApiResponse.ok(Map.of("url", url), "본문 이미지가 업로드되었습니다.");
    }

    @PutMapping("/reservations/{reservationId}")
    public ApiResponse<Map<String, Object>> updateReservation(@PathVariable String reservationId,
                                                              @RequestBody ReservationDTO reservationDTO) {
        ReservationEntity updated = reservationService.updateReservation(reservationId, reservationDTO);
        return ApiResponse.ok(toReservationMap(updated), "예약이 수정되었습니다.");
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ApiResponse<Void> deleteReservation(@PathVariable String reservationId) {
        reservationService.deleteReservation(reservationId);
        return ApiResponse.ok(null, "예약이 삭제되었습니다.");
    }

    @PutMapping("/users/{userId}")
    public ApiResponse<Map<String, Object>> updateUser(@PathVariable String userId,
                                                       @RequestBody Map<String, Object> payload) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.setName(stringValue(payload.get("name"), user.getName()));
        user.setEmail(stringValue(payload.get("email"), user.getEmail()));
        user.setPhoneNumber(stringValue(payload.get("phoneNumber"), user.getPhoneNumber()));
        user.setGender(stringValue(payload.get("gender"), user.getGender()));
        user.setBirthdate(stringValue(payload.get("birthdate"), user.getBirthdate()));
        user.setMemo(stringValue(payload.get("memo"), user.getMemo()));
        user.setRole(normalizeRole(stringValue(payload.get("role"), user.getRole())));
        user.setMaritalStatus(booleanValue(payload.get("maritalStatus"), user.isMaritalStatus()));
        user.setPackageCount(intValue(payload.get("packageCount"), user.getPackageCount()));
        user.setMileageBalance(intValue(payload.get("mileageBalance"), user.getMileageBalance() == null ? 0 : user.getMileageBalance()));

        return ApiResponse.ok(toUserMap(userRepository.save(user)), "회원 정보가 수정되었습니다.");
    }

    @DeleteMapping("/users/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable String userId, Authentication authentication) {
        if (authentication != null && userId.equals(authentication.getName())) {
            throw new IllegalArgumentException("현재 로그인한 관리자 계정은 삭제할 수 없습니다.");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("삭제할 회원을 찾을 수 없습니다: " + userId);
        }
        userRepository.deleteById(userId);
        return ApiResponse.ok(null, "회원이 삭제되었습니다.");
    }

    @GetMapping("/mileage-setting")
    public ApiResponse<Map<String, Object>> mileageSetting() {
        return ApiResponse.ok(toMileageSettingMap(mileageService.getSetting()));
    }

    @PutMapping("/mileage-setting")
    public ApiResponse<Map<String, Object>> updateMileageSetting(@RequestBody Map<String, Object> payload) {
        MileageSettingEntity setting = mileageService.updateRate(doubleValue(payload.get("earningRatePercent"), 0.0));
        return ApiResponse.ok(toMileageSettingMap(setting), "마일리지 적립률이 저장되었습니다.");
    }

    @GetMapping("/coupons")
    public ApiResponse<List<Map<String, Object>>> coupons() {
        return ApiResponse.ok(couponService.findAll().stream().map(this::toCouponMap).toList());
    }

    @PostMapping("/coupons")
    public ApiResponse<Map<String, Object>> createCoupon(@RequestBody Map<String, Object> payload) {
        CouponEntity coupon = couponService.issueCoupon(
                stringValue(payload.get("userId"), ""),
                stringValue(payload.get("name"), "쿠폰"),
                intValue(payload.get("discountAmount"), 0),
                stringValue(payload.get("status"), "AVAILABLE"),
                dateTimeValue(payload.get("expiresAt"))
        );
        return ApiResponse.ok(toCouponMap(coupon), "쿠폰이 발급되었습니다.");
    }

    @PutMapping("/coupons/{couponId}")
    public ApiResponse<Map<String, Object>> updateCoupon(@PathVariable Long couponId, @RequestBody Map<String, Object> payload) {
        CouponEntity coupon = couponService.updateCoupon(
                couponId,
                stringValue(payload.get("userId"), ""),
                stringValue(payload.get("name"), "쿠폰"),
                intValue(payload.get("discountAmount"), 0),
                stringValue(payload.get("status"), "AVAILABLE"),
                dateTimeValue(payload.get("expiresAt"))
        );
        return ApiResponse.ok(toCouponMap(coupon), "쿠폰이 수정되었습니다.");
    }

    @DeleteMapping("/coupons/{couponId}")
    public ApiResponse<Void> deleteCoupon(@PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
        return ApiResponse.ok(null, "쿠폰이 삭제되었습니다.");
    }

    private Map<String, Object> toUserMap(UserEntity user) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("userId", user.getUserId());
        item.put("name", user.getName());
        item.put("email", user.getEmail());
        item.put("phoneNumber", user.getPhoneNumber());
        item.put("role", user.getRole());
        item.put("gender", user.getGender());
        item.put("birthdate", user.getBirthdate());
        item.put("maritalStatus", user.isMaritalStatus());
        item.put("packageCount", user.getPackageCount());
        item.put("mileageBalance", user.getMileageBalance() == null ? 0 : user.getMileageBalance());
        item.put("memo", user.getMemo());
        item.put("reservationStatusCounts", reservationStatusCounts(user.getUserId()));
        return item;
    }

    private Map<String, Object> toReservationMap(ReservationEntity reservation) {
        String memberPhoneNumber = "";
        if (reservation.getUserId() != null && !reservation.getUserId().isBlank()) {
            memberPhoneNumber = userRepository.findById(reservation.getUserId())
                    .map(UserEntity::getPhoneNumber)
                    .orElse("");
        }
        return toReservationMap(reservation, memberPhoneNumber);
    }

    private Map<String, Object> toReservationMap(ReservationEntity reservation, String memberPhoneNumber) {
        String guestPhoneNumber = reservation.getPhoneNumber() == null ? "" : reservation.getPhoneNumber();
        String contactPhoneNumber = guestPhoneNumber.isBlank()
                ? (memberPhoneNumber == null ? "" : memberPhoneNumber)
                : guestPhoneNumber;

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", reservation.getId());
        item.put("userId", reservation.getUserId() == null ? "" : reservation.getUserId());
        item.put("name", reservation.getName() == null ? "" : reservation.getName());
        item.put("phoneNumber", guestPhoneNumber);
        item.put("contactPhoneNumber", contactPhoneNumber);
        item.put("status", reservation.getStatus() == null ? "" : reservation.getStatus());
        item.put("statusLabel", statusLabel(reservation.getStatus()));
        item.put("reservationDateTime", reservation.getReservationDateTime());
        item.put("courseId", reservation.getCourse() == null ? null : reservation.getCourse().getId());
        item.put("courseName", reservation.getCourse() == null ? "" : reservation.getCourse().getName());
        StaffEntity staff = reservation.getStaff() != null ? reservation.getStaff() : reservation.getCourse() == null ? null : reservation.getCourse().getStaff();
        item.put("staffId", staff == null ? null : staff.getId());
        item.put("staffName", staff == null ? "" : staff.getName());
        item.put("couponId", reservation.getCouponId());
        item.put("couponName", reservation.getCouponName() == null ? "" : reservation.getCouponName());
        item.put("couponDiscountAmount", reservation.getCouponDiscountAmount() == null ? 0 : reservation.getCouponDiscountAmount());
        item.put("mileageEarned", reservation.getMileageEarned() == null ? 0 : reservation.getMileageEarned());
        return item;
    }

    private Map<String, Object> toStaffMap(StaffEntity staff) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", staff.getId());
        item.put("name", staff.getName());
        item.put("profilePicture", staff.getProfilePicture());
        item.put("description", staff.getDescription());
        return item;
    }

    private Map<String, Object> toCouponMap(CouponEntity coupon) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", coupon.getId());
        item.put("userId", coupon.getUserId());
        item.put("name", coupon.getName());
        item.put("discountAmount", coupon.getDiscountAmount());
        item.put("status", coupon.getStatus());
        item.put("expiresAt", coupon.getExpiresAt());
        item.put("createdAt", coupon.getCreatedAt());
        return item;
    }

    private Map<String, Object> toMileageSettingMap(MileageSettingEntity setting) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("earningRatePercent", setting.getEarningRatePercent() == null ? 0.0 : setting.getEarningRatePercent());
        return item;
    }

    private AnnouncementDTO toAnnouncementDto(AnnouncementEntity entity) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setAttachmentPaths(entity.getAttachmentPaths());
        dto.setOriginalAttachmentNames(entity.getOriginalAttachmentNames());
        return dto;
    }

    private String stringValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = String.valueOf(value).trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private double doubleValue(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private LocalDateTime dateTimeValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return LocalDateTime.parse(String.valueOf(value));
    }

    private boolean booleanValue(Object value, boolean fallback) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value == null) {
            return fallback;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private String normalizeRole(String role) {
        String normalized = role == null ? "USER" : role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        return "ADMIN".equals(normalized) ? "ADMIN" : "USER";
    }

    private Map<String, Long> reservationStatusCounts(String userId) {
        Map<String, Long> counts = new LinkedHashMap<>();
        RESERVATION_STATUSES.forEach(status -> counts.put(status, 0L));

        reservationRepository.countStatusesByUserId(userId).forEach(row -> {
            String status = row[0] == null ? "" : String.valueOf(row[0]).trim().toUpperCase();
            Long count = row[1] instanceof Number number ? number.longValue() : 0L;
            if (!status.isBlank()) {
                counts.put(status, count);
            }
        });

        return counts;
    }

    private String statusLabel(String status) {
        if (status == null || status.isBlank()) {
            return "예약 대기";
        }
        return switch (status.trim().toUpperCase()) {
            case "PENDING" -> "예약 대기";
            case "CONFIRMED" -> "예약 확정";
            case "COMPLETED" -> "이용 완료";
            case "CANCELLED", "CANCELLED_USER" -> "회원 취소";
            case "CANCELLED_ADMIN" -> "관리자 취소";
            case "NO_SHOW" -> "노쇼";
            default -> status;
        };
    }
}
