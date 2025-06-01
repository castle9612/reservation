package com.reservation.web.service;

import com.reservation.web.entity.AnnouncementEntity;
import com.reservation.web.repository.AnnouncementRepository;
import org.slf4j.Logger; // SLF4J 로거 사용
import org.slf4j.LoggerFactory; // SLF4J 로거 사용
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
// import java.util.Iterator; // Iterator 사용 부분은 개선된 로직으로 대체
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.Collections; // Collections.reverseOrder() 사용

@Service
public class AnnouncementService {
    private static final Logger logger = LoggerFactory.getLogger(AnnouncementService.class);
    private final AnnouncementRepository announcementRepository;
    private final Path rootFileLocation;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               @Value("${file.upload-dir}") String uploadDir) {
        this.announcementRepository = announcementRepository;
        this.rootFileLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootFileLocation);
            logger.info("Upload directory created/ensured at: {}", this.rootFileLocation);
        } catch (IOException ex) {
            logger.error("Could not create upload directory! Path: {}", this.rootFileLocation, ex);
            throw new RuntimeException("Could not create upload directory! Path: " + this.rootFileLocation, ex);
        }
    }

    public List<AnnouncementEntity> findAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public AnnouncementEntity save(AnnouncementEntity announcement, MultipartFile[] attachmentFiles) throws IOException {
        List<String> attachmentPaths = new ArrayList<>();
        List<String> originalFileNames = new ArrayList<>();

        if (attachmentFiles != null && attachmentFiles.length > 0) {
            for (MultipartFile file : attachmentFiles) {
                if (file != null && !file.isEmpty()) {
                    String originalFileName = file.getOriginalFilename();
                    String storedFileName = storeFile(file);

                    attachmentPaths.add("/uploads/" + storedFileName);
                    originalFileNames.add(originalFileName != null ? originalFileName : storedFileName);
                }
            }
        }
        announcement.setAttachmentPaths(attachmentPaths);
        announcement.setOriginalAttachmentNames(originalFileNames);
        return announcementRepository.save(announcement);
    }

    @Transactional
    public AnnouncementEntity update(Long id, AnnouncementEntity updatedAnnouncementData, MultipartFile[] newAttachmentFiles, List<String> deletedAttachmentPaths) throws IOException {
        logger.info("Service update method called for id: {}. Requested deleted paths: {}", id, deletedAttachmentPaths);

        AnnouncementEntity existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Update failed: Announcement not found with id: {}", id);
                    return new IllegalArgumentException("Invalid announcement Id:" + id);
                });

        existingAnnouncement.setTitle(updatedAnnouncementData.getTitle());
        existingAnnouncement.setContent(updatedAnnouncementData.getContent());

        List<String> currentAttachmentPaths = existingAnnouncement.getAttachmentPaths() != null ? new ArrayList<>(existingAnnouncement.getAttachmentPaths()) : new ArrayList<>();
        List<String> currentOriginalNames = existingAnnouncement.getOriginalAttachmentNames() != null ? new ArrayList<>(existingAnnouncement.getOriginalAttachmentNames()) : new ArrayList<>();
        logger.debug("Before update - Current paths: {}, Current original names: {}", currentAttachmentPaths, currentOriginalNames);


        // 삭제할 파일 처리
        if (deletedAttachmentPaths != null && !deletedAttachmentPaths.isEmpty()) {
            logger.debug("Processing deletedAttachmentPaths: {}", deletedAttachmentPaths);
            List<String> pathsToRemoveFromServer = new ArrayList<>();
            List<Integer> indicesToRemoveFromLists = new ArrayList<>();

            for (String pathToDelete : deletedAttachmentPaths) { // 사용자가 삭제 요청한 경로들
                logger.debug("Attempting to mark for deletion: [{}] (length: {})", pathToDelete, pathToDelete != null ? pathToDelete.length() : "null");
                for (int i = 0; i < currentAttachmentPaths.size(); i++) {
                    String currentPath = currentAttachmentPaths.get(i);
                    logger.debug("Comparing with current path: [{}] (length: {}) at index {}", currentPath, currentPath != null ? currentPath.length() : "null", i);
                    if (currentPath != null && currentPath.equals(pathToDelete)) {
                        logger.info("Match found! Marking path {} (index {}) for deletion.", currentPath, i);
                        if (!indicesToRemoveFromLists.contains(i)) { // 중복 추가 방지
                            indicesToRemoveFromLists.add(i);
                            pathsToRemoveFromServer.add(pathToDelete);
                        }
                        break;
                    }
                }
            }
            logger.debug("Indices to remove from lists: {}", indicesToRemoveFromLists);
            logger.debug("Paths to remove from server: {}", pathsToRemoveFromServer);


            // 인덱스 기반 삭제는 역순으로 진행해야 정확합니다.
            Collections.sort(indicesToRemoveFromLists, Collections.reverseOrder()); // java.util.Collections.reverseOrder()

            for (int indexToRemove : indicesToRemoveFromLists) {
                if (indexToRemove < currentAttachmentPaths.size()) {
                    logger.debug("Removing from currentAttachmentPaths at index {}: {}", indexToRemove, currentAttachmentPaths.get(indexToRemove));
                    currentAttachmentPaths.remove(indexToRemove);
                }
                if (indexToRemove < currentOriginalNames.size()) { // 방어 코드
                    logger.debug("Removing from currentOriginalNames at index {}: {}", indexToRemove, currentOriginalNames.get(indexToRemove));
                    currentOriginalNames.remove(indexToRemove);
                }
            }

            for (String serverPath : pathsToRemoveFromServer) {
                try {
                    deleteFileFromServer(serverPath);
                } catch (IOException e) {
                    logger.error("Error deleting file from server during update: {}", serverPath, e);
                    throw e; // 트랜잭션 롤백 유도
                }
            }
        }


        // 새 첨부파일 처리
        if (newAttachmentFiles != null && newAttachmentFiles.length > 0) {
            logger.debug("Processing new attachment files. Count: {}", newAttachmentFiles.length);
            for (MultipartFile file : newAttachmentFiles) {
                if (file != null && !file.isEmpty()) {
                    String originalFileName = file.getOriginalFilename();
                    logger.debug("New file - Original name: {}", originalFileName);
                    String storedFileName = storeFile(file); // IOException 가능
                    currentAttachmentPaths.add("/uploads/" + storedFileName);
                    currentOriginalNames.add(originalFileName != null ? originalFileName : storedFileName);
                }
            }
        }

        logger.debug("After update - Current paths: {}, Current original names: {}", currentAttachmentPaths, currentOriginalNames);
        existingAnnouncement.setAttachmentPaths(currentAttachmentPaths);
        existingAnnouncement.setOriginalAttachmentNames(currentOriginalNames);

        AnnouncementEntity savedEntity = announcementRepository.save(existingAnnouncement);
        logger.info("Announcement id: {} updated successfully.", id);
        return savedEntity;
    }

    @Transactional
    public void delete(Long id) {
        Optional<AnnouncementEntity> announcementOpt = announcementRepository.findById(id);
        if (announcementOpt.isPresent()) {
            AnnouncementEntity announcement = announcementOpt.get();
            if (announcement.getAttachmentPaths() != null) {
                for (String path : announcement.getAttachmentPaths()) {
                    try {
                        deleteFileFromServer(path);
                    } catch (IOException e) {
                        logger.error("Failed to delete attachment file during announcement deletion: {} for announcement id: {}", path, id, e);
                        // 게시글 삭제 시 개별 파일 삭제 실패가 전체 삭제를 막아야 하는지에 대한 정책 필요
                        // 여기서는 로깅만 하고 계속 진행
                    }
                }
            }
            announcementRepository.deleteById(id);
            logger.info("Deleted announcement with id: {}", id);
        } else {
            logger.warn("Attempted to delete non-existing announcement with id: {}", id);
        }
    }

    public AnnouncementEntity findById(Long id) {
        return announcementRepository.findById(id).orElse(null);
    }

    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            logger.warn("Attempted to store an empty file.");
            throw new IOException("Failed to store empty file.");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            originalFileName = "unknown_file_" + UUID.randomUUID().toString();
            logger.info("Original file name was null or blank, generated: {}", originalFileName);
        }
        // Sanitize filename (optional, but good practice)
        // originalFileName = org.springframework.util.StringUtils.cleanPath(originalFileName);


        String extension = "";
        int lastDotIndex = originalFileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < originalFileName.length() - 1) {
            extension = originalFileName.substring(lastDotIndex + 1).toLowerCase();
        }

        String storedFileNameBase = UUID.randomUUID().toString();
        String storedFileName = extension.isEmpty() ? storedFileNameBase : storedFileNameBase + "." + extension;

        Path targetLocation = this.rootFileLocation.resolve(storedFileName);
        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Stored file {} as {}", originalFileName, storedFileName);
        } catch (IOException e) {
            logger.error("Failed to store file {} as {}. Path: {}", originalFileName, storedFileName, targetLocation, e);
            throw e;
        }
        return storedFileName;
    }

    private void deleteFileFromServer(String filePathWithContext) throws IOException {
        if (filePathWithContext != null && filePathWithContext.startsWith("/uploads/")) {
            String fileName = filePathWithContext.substring("/uploads/".length());
            Path targetLocation = this.rootFileLocation.resolve(fileName);
            try {
                boolean deleted = Files.deleteIfExists(targetLocation);
                if (deleted) {
                    logger.info("Deleted physical file: {}", targetLocation);
                } else {
                    logger.warn("Attempted to delete non-existing physical file: {}", targetLocation);
                }
            } catch (IOException e) {
                logger.error("Failed to delete physical file: {}", targetLocation, e);
                throw e;
            }
        } else {
            logger.warn("Invalid file path for deletion: {}", filePathWithContext);
        }
    }

    public String storeSummernoteImage(MultipartFile multipartFile) throws IOException {
        String storedFileName = storeFile(multipartFile);
        return "/uploads/" + storedFileName;
    }
}