package com.reservation.web.service;

import com.reservation.web.dto.ReviewDTO;
import com.reservation.web.entity.ReviewEntity;
import com.reservation.web.entity.UserEntity;
import com.reservation.web.repository.ReviewRepository;
import com.reservation.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final Path uploadRoot;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         @Value("${file.upload-dir}") String uploadDir) throws IOException {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadRoot);
    }

    @Transactional(readOnly = true)
    public List<ReviewEntity> findAll() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public ReviewEntity create(String reviewerName,
                               String reviewerUserId,
                               Integer rating,
                               String content,
                               MultipartFile[] images) throws IOException {
        validateReviewInput(reviewerName, rating, content);

        ReviewEntity review = new ReviewEntity();
        review.setReviewerName(resolveReviewerName(reviewerName, reviewerUserId));
        review.setReviewerUserId(trimToNull(reviewerUserId));
        review.setRating(rating);
        review.setContent(content.trim());
        review.setImagePaths(storeImages(images));
        return reviewRepository.save(review);
    }

    @Transactional
    public ReviewEntity update(Long reviewId,
                               String currentUserId,
                               boolean admin,
                               Integer rating,
                               String content,
                               MultipartFile[] images) throws IOException {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        validateReviewOwnership(review, currentUserId, admin);
        validateReviewInput(review.getReviewerName(), rating, content);

        review.setRating(rating);
        review.setContent(content.trim());
        if (images != null && images.length > 0 && !allEmpty(images)) {
            review.setImagePaths(storeImages(images));
        }

        return reviewRepository.save(review);
    }

    @Transactional
    public void delete(Long reviewId, String currentUserId, boolean admin) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        validateReviewOwnership(review, currentUserId, admin);
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> toDtos(List<ReviewEntity> entities, String currentUserId, boolean admin) {
        return entities.stream().map(entity -> toDto(entity, currentUserId, admin)).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> buildSummary(List<ReviewEntity> reviews) {
        double average = reviews.stream().mapToInt(ReviewEntity::getRating).average().orElse(0.0);
        Map<Integer, Long> counts = new LinkedHashMap<>();
        for (int rating = 5; rating >= 1; rating--) {
            int current = rating;
            counts.put(rating, reviews.stream().filter(review -> review.getRating() == current).count());
        }
        List<String> recentImages = reviews.stream().flatMap(review -> review.getImagePaths().stream()).limit(8).toList();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("averageRating", average);
        summary.put("totalReviews", reviews.size());
        summary.put("ratingCounts", counts);
        summary.put("recentImages", recentImages);
        return summary;
    }

    private ReviewDTO toDto(ReviewEntity entity, String currentUserId, boolean admin) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(entity.getId());
        dto.setReviewerName(entity.getReviewerName());
        dto.setReviewerUserId(entity.getReviewerUserId());
        dto.setRating(entity.getRating());
        dto.setContent(entity.getContent());
        dto.setImagePaths(entity.getImagePaths());
        dto.setCreatedAt(entity.getCreatedAt());
        boolean editable = admin || (currentUserId != null && currentUserId.equals(entity.getReviewerUserId()));
        dto.setEditable(editable);
        dto.setDeletable(editable);
        return dto;
    }

    private void validateReviewInput(String reviewerName, Integer rating, String content) {
        if (reviewerName == null || reviewerName.isBlank()) {
            throw new IllegalArgumentException("리뷰 작성자 이름은 필수입니다.");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("별점은 1점부터 5점까지 입력해 주세요.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해 주세요.");
        }
    }

    private String resolveReviewerName(String reviewerName, String reviewerUserId) {
        String userId = trimToNull(reviewerUserId);
        if (userId == null) {
            return reviewerName.trim();
        }
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getName();
    }

    private void validateReviewOwnership(ReviewEntity review, String currentUserId, boolean admin) {
        if (admin) {
            return;
        }
        if (currentUserId == null || !currentUserId.equals(review.getReviewerUserId())) {
            throw new IllegalArgumentException("리뷰를 수정하거나 삭제할 권한이 없습니다.");
        }
    }

    private List<String> storeImages(MultipartFile[] images) throws IOException {
        List<String> imagePaths = new ArrayList<>();
        if (images == null) {
            return imagePaths;
        }
        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                continue;
            }

            String originalFileName = StringUtils.cleanPath(image.getOriginalFilename() == null ? "" : image.getOriginalFilename());
            validateImageFile(image, originalFileName);

            String extension = "";
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalFileName.length() - 1) {
                extension = originalFileName.substring(dotIndex);
            }

            String storedFileName = "review-" + UUID.randomUUID() + extension;
            Path target = uploadRoot.resolve(storedFileName).normalize();
            if (!target.startsWith(uploadRoot)) {
                throw new IOException("잘못된 리뷰 이미지 경로입니다.");
            }
            try (var inputStream = image.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            imagePaths.add("/uploads/" + storedFileName);
        }
        return imagePaths;
    }

    private boolean allEmpty(MultipartFile[] images) {
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private void validateImageFile(MultipartFile image, String originalFileName) throws IOException {
        if (originalFileName.contains("..")) {
            throw new IOException("잘못된 파일 이름입니다.");
        }

        UploadFileValidator.validateImage(image, originalFileName);
    }
}
