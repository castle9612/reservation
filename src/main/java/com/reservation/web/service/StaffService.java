package com.reservation.web.service;

import com.reservation.web.dto.StaffDTO;
import com.reservation.web.entity.StaffEntity;
import com.reservation.web.repository.StaffRepository;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StaffService {
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final List<String> ALLOWED_IMAGE_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final StaffRepository staffRepository;
    private final Path uploadRoot;

    public StaffService(StaffRepository staffRepository,
                        @Value("${file.upload-dir}") String uploadDir) throws IOException {
        this.staffRepository = staffRepository;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadRoot);
    }

    private StaffDTO convertEntityToDTO(StaffEntity staffEntity) {
        return new StaffDTO(
                staffEntity.getId(),
                staffEntity.getName(),
                staffEntity.getProfilePicture()
        );
    }

    @Transactional(readOnly = true)
    public List<StaffEntity> findAllStaffEntities() {
        return staffRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StaffDTO> findAllStaff() {
        return staffRepository.findAll().stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StaffEntity findEntityById(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 직원이 존재하지 않습니다: " + id));
    }

    @Transactional(readOnly = true)
    public StaffDTO findById(Long id) {
        StaffEntity staffEntity = staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 직원이 존재하지 않습니다: " + id));
        return convertEntityToDTO(staffEntity);
    }

    @Transactional
    public StaffEntity saveStaff(StaffDTO staffDTO, MultipartFile profileImage) throws IOException {
        StaffEntity staffEntity;
        if (staffDTO.getId() != null) {
            staffEntity = staffRepository.findById(staffDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("수정할 직원을 찾을 수 없습니다: " + staffDTO.getId()));
        } else {
            staffEntity = new StaffEntity();
        }
        staffEntity.setName(staffDTO.getName());

        if (profileImage != null && !profileImage.isEmpty()) {
            String storedFileName = storeProfileImage(profileImage);
            staffEntity.setProfilePicture("/uploads/" + storedFileName);
        } else if (staffDTO.getProfilePicture() != null && !staffDTO.getProfilePicture().isBlank()) {
            staffEntity.setProfilePicture(staffDTO.getProfilePicture());
        }

        return staffRepository.save(staffEntity);
    }

    @Transactional
    public void deleteStaff(Long id) {
        if (!staffRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 직원을 찾을 수 없습니다: " + id);
        }
        staffRepository.deleteById(id);
    }

    private String storeProfileImage(MultipartFile profileImage) throws IOException {
        String originalFileName = StringUtils.cleanPath(profileImage.getOriginalFilename() == null ? "" : profileImage.getOriginalFilename());
        validateImageFile(profileImage, originalFileName);

        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < originalFileName.length() - 1) {
            extension = originalFileName.substring(dotIndex);
        }

        String storedFileName = UUID.randomUUID() + extension;
        Path target = uploadRoot.resolve(storedFileName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new IOException("잘못된 업로드 경로입니다.");
        }

        try (var inputStream = profileImage.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedFileName;
    }

    private void validateImageFile(MultipartFile image, String originalFileName) throws IOException {
        if (originalFileName.contains("..")) {
            throw new IOException("잘못된 파일 이름입니다.");
        }

        String lowerName = originalFileName.toLowerCase();
        boolean allowedExtension = ALLOWED_IMAGE_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
        if (!allowedExtension) {
            throw new IOException("이미지 파일만 업로드할 수 있습니다.");
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IOException("허용되지 않는 이미지 형식입니다.");
        }
    }
}
