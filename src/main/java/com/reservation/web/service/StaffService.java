package com.reservation.web.service;

import com.reservation.web.dto.StaffDTO;
import com.reservation.web.entity.StaffEntity;
import com.reservation.web.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true) // 데이터 변경이 없으므로 readOnly
    public List<StaffEntity> findAllStaffEntities() { // 메서드명 명확화
        return staffRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StaffDTO> findAllStaff() {
        return staffRepository.findAll().stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public StaffEntity findEntityById(Long id) { // Entity 반환
        return staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 직원이 존재하지 않습니다: " + id));
    }

    @Transactional(readOnly = true)
    public StaffDTO findById(Long id) { // DTO 반환
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
}
