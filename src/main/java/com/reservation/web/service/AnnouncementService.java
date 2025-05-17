package com.reservation.web.service;

import com.reservation.web.entity.AnnouncementEntity;
import com.reservation.web.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private final Path rootFileLocation; // 파일 저장 루트 경로

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               @Value("${file.upload-dir}") String uploadDir) { // application.properties에서 경로 주입
        this.announcementRepository = announcementRepository;
        this.rootFileLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootFileLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    public List<AnnouncementEntity> findAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc(); // 최신순 정렬 예시
    }

    @Transactional
    public AnnouncementEntity save(AnnouncementEntity announcement, MultipartFile[] attachmentFiles) throws IOException {
        announcement.setCreatedAt(LocalDateTime.now());

        if (attachmentFiles != null && attachmentFiles.length > 0) {
            List<String> attachmentPaths = new ArrayList<>();
            for (MultipartFile file : attachmentFiles) {
                if (file != null && !file.isEmpty()) {
                    String fileName = storeFile(file);
                    attachmentPaths.add("/uploads/" + fileName); // 웹 접근 경로
                }
            }
            announcement.setAttachmentPaths(attachmentPaths);
        }
        return announcementRepository.save(announcement);
    }

    @Transactional
    public AnnouncementEntity update(Long id, AnnouncementEntity updatedAnnouncementData, MultipartFile[] newAttachmentFiles, List<String> deletedAttachmentPaths) throws IOException {
        AnnouncementEntity existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid announcement Id:" + id));

        existingAnnouncement.setTitle(updatedAnnouncementData.getTitle());
        existingAnnouncement.setContent(updatedAnnouncementData.getContent());

        List<String> currentAttachmentPaths = existingAnnouncement.getAttachmentPaths() != null ? new ArrayList<>(existingAnnouncement.getAttachmentPaths()) : new ArrayList<>();

        if (deletedAttachmentPaths != null && !deletedAttachmentPaths.isEmpty()) {
            for (String pathToDelete : deletedAttachmentPaths) {
                deleteFileFromServer(pathToDelete);
                currentAttachmentPaths.remove(pathToDelete);
            }
        }

        if (newAttachmentFiles != null && newAttachmentFiles.length > 0) {
            for (MultipartFile file : newAttachmentFiles) {
                if (file != null && !file.isEmpty()) {
                    String fileName = storeFile(file);
                    currentAttachmentPaths.add("/uploads/" + fileName);
                }
            }
        }
        existingAnnouncement.setAttachmentPaths(currentAttachmentPaths);
        return announcementRepository.save(existingAnnouncement);
    }

    @Transactional
    public void delete(Long id) {
        AnnouncementEntity announcement = findById(id);
        if (announcement != null && announcement.getAttachmentPaths() != null) {
            for (String path : announcement.getAttachmentPaths()) {
                try {
                    deleteFileFromServer(path);
                } catch (IOException e) {
                    System.err.println("Failed to delete attachment file: " + path + " - " + e.getMessage());
                }
            }
        }
        announcementRepository.deleteById(id);
    }

    public AnnouncementEntity findById(Long id) {
        return announcementRepository.findById(id).orElse(null);
    }

    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;
        Path targetLocation = this.rootFileLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    private void deleteFileFromServer(String filePathWithContext) throws IOException {
        if (filePathWithContext != null && filePathWithContext.startsWith("/uploads/")) {
            String fileName = filePathWithContext.substring("/uploads/".length());
            Path targetLocation = this.rootFileLocation.resolve(fileName);
            Files.deleteIfExists(targetLocation);
        }
    }

    // Summernote 이미지 업로드용 메소드
    public String storeSummernoteImage(MultipartFile multipartFile) throws IOException {
        String fileName = storeFile(multipartFile);
        return "/uploads/" + fileName; // 웹 접근 경로 반환
    }
}

// AnnouncementRepository에 추가 (최신순 정렬)
// List<AnnouncementEntity> findAllByOrderByCreatedAtDesc();