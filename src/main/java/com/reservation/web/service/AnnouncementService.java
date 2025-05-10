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

    // ⬇️ 이 부분: 프로젝트 루트의 'uploads' 폴더를 사용하도록 경로 정의
    private final Path UPLOAD_DIR_PATH = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath();

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public List<AnnouncementEntity> findAll() {
        return announcementRepository.findAll();
    }

    public AnnouncementEntity save(AnnouncementEntity announcement, MultipartFile imageFile) throws IOException {
        // ⬇️ 파일 업로드 로직 시작
        if (imageFile != null && !imageFile.isEmpty()) {
            // 디렉토리 생성 (없으면)
            Files.createDirectories(UPLOAD_DIR_PATH);

            // 파일명 중복 방지를 위해 현재 시간 추가
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = UPLOAD_DIR_PATH.resolve(fileName);

            // 파일 저장
            Files.write(filePath, imageFile.getBytes());

            // DB에 저장될 웹 접근 경로 설정
            announcement.setImagePath("/uploads/" + fileName);
        }
        // ⬆️ 파일 업로드 로직 끝

        announcement.setCreatedAt(LocalDateTime.now());
        return announcementRepository.save(announcement);
    }

    public AnnouncementEntity update(Long id, AnnouncementEntity updatedAnnouncement, MultipartFile imageFile) throws IOException {
        AnnouncementEntity existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid announcement Id:" + id));

        existingAnnouncement.setTitle(updatedAnnouncement.getTitle());
        existingAnnouncement.setContent(updatedAnnouncement.getContent());

        // ⬇️ 파일 업로드 로직 시작 (새로운 파일이 제공된 경우에만 처리)
        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 파일 삭제 로직 (선택 사항)
             if (existingAnnouncement.getImagePath() != null && !existingAnnouncement.getImagePath().isEmpty()) {
                 try {
                     Path oldFilePath = UPLOAD_DIR_PATH.resolve(existingAnnouncement.getImagePath().substring("/uploads/".length()));
                     Files.deleteIfExists(oldFilePath);
                 } catch (IOException e) {
                     // 로깅 또는 예외 처리
                     System.err.println("Failed to delete old image: " + e.getMessage());
                 }
             }

            Files.createDirectories(UPLOAD_DIR_PATH); // 디렉토리 생성 (없으면)

            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = UPLOAD_DIR_PATH.resolve(fileName);

            Files.write(filePath, imageFile.getBytes());

            existingAnnouncement.setImagePath("/uploads/" + fileName); // 새 이미지 경로로 업데이트
        }
        // ⬆️ 파일 업로드 로직 끝

        // 수정 시에는 createdAt은 변경하지 않음 (보통 updated_at 필드를 사용하지만 현재 Entity에는 없음)
        return announcementRepository.save(existingAnnouncement);
    }

    public void delete(Long id) {
        // 파일 삭제 로직 (선택 사항)
         AnnouncementEntity announcement = findById(id);
         if (announcement != null && announcement.getImagePath() != null && !announcement.getImagePath().isEmpty()) {
             try {
                 Path filePath = UPLOAD_DIR_PATH.resolve(announcement.getImagePath().substring("/uploads/".length()));
                 Files.deleteIfExists(filePath);
             } catch (IOException e) {
                 System.err.println("Failed to delete image on announcement deletion: " + e.getMessage());
             }
         }
        announcementRepository.deleteById(id);
    }

    public AnnouncementEntity findById(Long id) {
        return announcementRepository.findById(id).orElse(null);
    }
}