package com.reservation.web.service;

import com.reservation.web.dto.SiteContentDTO;
import com.reservation.web.entity.SiteContentEntity;
import com.reservation.web.repository.SiteContentRepository;
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
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SiteContentService {

    private static final long SINGLE_CONTENT_ID = 1L;

    private final SiteContentRepository siteContentRepository;
    private final Path uploadRoot;

    public SiteContentService(SiteContentRepository siteContentRepository,
                              @Value("${file.upload-dir}") String uploadDir) throws IOException {
        this.siteContentRepository = siteContentRepository;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadRoot);
    }

    @Transactional
    public SiteContentDTO getContent() {
        return toDto(getOrCreateContent());
    }

    @Transactional
    public SiteContentDTO updateContent(SiteContentDTO dto, MultipartFile heroImage) throws IOException {
        SiteContentEntity content = getOrCreateContent();

        content.setBrandName(valueOrDefault(dto.getBrandName(), content.getBrandName()));
        content.setHeroEyebrow(valueOrDefault(dto.getHeroEyebrow(), content.getHeroEyebrow()));
        content.setHeroTitle(valueOrDefault(dto.getHeroTitle(), content.getHeroTitle()));
        content.setHeroDescription(valueOrDefault(dto.getHeroDescription(), content.getHeroDescription()));
        content.setStoreName(valueOrDefault(dto.getStoreName(), content.getStoreName()));
        content.setStoreAddress(valueOrDefault(dto.getStoreAddress(), content.getStoreAddress()));
        content.setStorePhone(trimToEmpty(dto.getStorePhone()));
        content.setLocationDescription(valueOrDefault(dto.getLocationDescription(), content.getLocationDescription()));

        if (heroImage != null && !heroImage.isEmpty()) {
            content.setHeroImagePath("/uploads/" + storeHeroImage(heroImage));
        } else if (dto.getHeroImagePath() != null) {
            content.setHeroImagePath(dto.getHeroImagePath().trim());
        }

        content.setUpdatedAt(LocalDateTime.now());
        return toDto(siteContentRepository.save(content));
    }

    private SiteContentEntity getOrCreateContent() {
        return siteContentRepository.findById(SINGLE_CONTENT_ID)
                .orElseGet(() -> {
                    SiteContentEntity entity = new SiteContentEntity();
                    entity.setId(SINGLE_CONTENT_ID);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return siteContentRepository.save(entity);
                });
    }

    private SiteContentDTO toDto(SiteContentEntity entity) {
        SiteContentDTO dto = new SiteContentDTO();
        dto.setBrandName(entity.getBrandName());
        dto.setHeroEyebrow(entity.getHeroEyebrow());
        dto.setHeroTitle(entity.getHeroTitle());
        dto.setHeroDescription(entity.getHeroDescription());
        dto.setHeroImagePath(entity.getHeroImagePath());
        dto.setStoreName(entity.getStoreName());
        dto.setStoreAddress(entity.getStoreAddress());
        dto.setStorePhone(entity.getStorePhone());
        dto.setLocationDescription(entity.getLocationDescription());
        return dto;
    }

    private String storeHeroImage(MultipartFile image) throws IOException {
        String originalFileName = StringUtils.cleanPath(image.getOriginalFilename() == null ? "" : image.getOriginalFilename());
        UploadFileValidator.validateImage(image, originalFileName);

        String extension = UploadFileValidator.extensionOf(originalFileName);
        String storedFileName = "hero-" + UUID.randomUUID() + "." + extension;
        Path target = uploadRoot.resolve(storedFileName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new IOException("잘못된 업로드 경로입니다.");
        }

        try (var inputStream = image.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return storedFileName;
    }

    private String valueOrDefault(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
