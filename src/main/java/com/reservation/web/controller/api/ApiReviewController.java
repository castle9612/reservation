package com.reservation.web.controller.api;

import com.reservation.web.entity.ReviewEntity;
import com.reservation.web.service.ReviewService;
import com.reservation.web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ApiReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    @GetMapping
    public ApiResponse<Map<String, Object>> listReviews(Authentication authentication) {
        String currentUserId = userService.getCurrentUserId();
        boolean admin = hasAdminRole(authentication);

        List<ReviewEntity> reviews = reviewService.findAll();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", reviewService.buildSummary(reviews));
        data.put("reviews", reviewService.toDtos(reviews, currentUserId, admin));
        return ApiResponse.ok(data);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> createReview(Authentication authentication,
                                          @RequestParam(required = false) String reviewerName,
                                          @RequestParam Integer rating,
                                          @RequestParam String content,
                                          @RequestParam(name = "images", required = false) MultipartFile[] images) throws IOException {
        String currentUserId = userService.getCurrentUserId();
        String effectiveName = currentUserId != null ? userService.getUserEntity(currentUserId).getName() : reviewerName;
        reviewService.create(effectiveName, currentUserId, rating, content, images);
        return ApiResponse.ok(null, "리뷰가 등록되었습니다.");
    }

    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> updateReview(@PathVariable Long reviewId,
                                          Authentication authentication,
                                          @RequestParam Integer rating,
                                          @RequestParam String content,
                                          @RequestParam(name = "images", required = false) MultipartFile[] images) throws IOException {
        reviewService.update(reviewId, userService.getCurrentUserId(), hasAdminRole(authentication), rating, content, images);
        return ApiResponse.ok(null, "리뷰가 수정되었습니다.");
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(@PathVariable Long reviewId, Authentication authentication) {
        reviewService.delete(reviewId, userService.getCurrentUserId(), hasAdminRole(authentication));
        return ApiResponse.ok(null, "리뷰가 삭제되었습니다.");
    }

    private boolean hasAdminRole(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream().anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
    }
}
