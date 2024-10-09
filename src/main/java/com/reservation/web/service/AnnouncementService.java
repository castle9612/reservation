package com.reservation.web.service;

import com.reservation.web.entity.AnnouncementEntity;
import com.reservation.web.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;

    private final String UPLOAD_DIR = "uploads/";

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public List<AnnouncementEntity> findAll() {
        return announcementRepository.findAll();
    }

    public AnnouncementEntity save(AnnouncementEntity announcement, MultipartFile imageFile) throws IOException {
        if (!imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, imageFile.getBytes());

            announcement.setImagePath("/uploads/" + fileName);  // 이미지 경로 설정
        }

        announcement.setCreatedAt(LocalDateTime.now());
        return announcementRepository.save(announcement);
    }

    public AnnouncementEntity update(Long id, AnnouncementEntity updatedAnnouncement, MultipartFile imageFile) throws IOException {
        AnnouncementEntity existingAnnouncement = announcementRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid announcement Id:" + id));

        existingAnnouncement.setTitle(updatedAnnouncement.getTitle());
        existingAnnouncement.setContent(updatedAnnouncement.getContent());

        if (!imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, imageFile.getBytes());

            existingAnnouncement.setImagePath("/uploads/" + fileName);  // 이미지 경로 설정
        }

        return announcementRepository.save(existingAnnouncement);
    }

    public void delete(Long id) {
        announcementRepository.deleteById(id);
    }

    public AnnouncementEntity findById(Long id) {
        return announcementRepository.findById(id).orElse(null);
    }
}



