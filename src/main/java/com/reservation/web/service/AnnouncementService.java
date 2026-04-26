package com.reservation.web.service;

import com.reservation.web.entity.AnnouncementEntity;
import com.reservation.web.repository.AnnouncementRepository;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AnnouncementService {
    private static final Logger logger = LoggerFactory.getLogger(AnnouncementService.class);
    private static final Safelist ANNOUNCEMENT_HTML_POLICY = Safelist.relaxed()
            .addTags("section", "article")
            .addAttributes(":all", "class")
            .addAttributes("a", "target", "rel")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf", "txt", "doc", "docx", "hwp", "hwpx"
    );

    private final AnnouncementRepository announcementRepository;
    private final Path rootFileLocation;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               @Value("${file.upload-dir}") String uploadDir) {
        this.announcementRepository = announcementRepository;
        this.rootFileLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootFileLocation);
            logger.info("Upload directory ready at {}", this.rootFileLocation);
        } catch (IOException ex) {
            logger.error("Could not create upload directory: {}", this.rootFileLocation, ex);
            throw new RuntimeException("Could not create upload directory: " + this.rootFileLocation, ex);
        }
    }

    public List<AnnouncementEntity> findAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public AnnouncementEntity save(AnnouncementEntity announcement, MultipartFile[] attachmentFiles) throws IOException {
        announcement.setContent(sanitizeHtml(announcement.getContent()));

        List<String> attachmentPaths = new ArrayList<>();
        List<String> originalFileNames = new ArrayList<>();

        if (attachmentFiles != null) {
            for (MultipartFile file : attachmentFiles) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                String originalFileName = cleanOriginalFilename(file);
                String storedFileName = storeFile(file);
                attachmentPaths.add("/uploads/" + storedFileName);
                originalFileNames.add(originalFileName);
            }
        }

        announcement.setAttachmentPaths(attachmentPaths);
        announcement.setOriginalAttachmentNames(originalFileNames);
        return announcementRepository.save(announcement);
    }

    @Transactional
    public AnnouncementEntity update(Long id,
                                     AnnouncementEntity updatedAnnouncementData,
                                     MultipartFile[] newAttachmentFiles,
                                     List<String> deletedAttachmentPaths) throws IOException {
        AnnouncementEntity existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. id=" + id));

        existingAnnouncement.setTitle(updatedAnnouncementData.getTitle());
        existingAnnouncement.setContent(sanitizeHtml(updatedAnnouncementData.getContent()));

        List<String> currentAttachmentPaths = existingAnnouncement.getAttachmentPaths() != null
                ? new ArrayList<>(existingAnnouncement.getAttachmentPaths())
                : new ArrayList<>();
        List<String> currentOriginalNames = existingAnnouncement.getOriginalAttachmentNames() != null
                ? new ArrayList<>(existingAnnouncement.getOriginalAttachmentNames())
                : new ArrayList<>();

        if (deletedAttachmentPaths != null && !deletedAttachmentPaths.isEmpty()) {
            removeDeletedAttachments(currentAttachmentPaths, currentOriginalNames, deletedAttachmentPaths);
        }

        if (newAttachmentFiles != null) {
            for (MultipartFile file : newAttachmentFiles) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                String originalFileName = cleanOriginalFilename(file);
                String storedFileName = storeFile(file);
                currentAttachmentPaths.add("/uploads/" + storedFileName);
                currentOriginalNames.add(originalFileName);
            }
        }

        existingAnnouncement.setAttachmentPaths(currentAttachmentPaths);
        existingAnnouncement.setOriginalAttachmentNames(currentOriginalNames);

        return announcementRepository.save(existingAnnouncement);
    }

    @Transactional
    public void delete(Long id) {
        Optional<AnnouncementEntity> announcementOpt = announcementRepository.findById(id);
        if (announcementOpt.isEmpty()) {
            logger.warn("Attempted to delete missing announcement id={}", id);
            return;
        }

        AnnouncementEntity announcement = announcementOpt.get();
        if (announcement.getAttachmentPaths() != null) {
            for (String path : announcement.getAttachmentPaths()) {
                try {
                    deleteFileFromServer(path);
                } catch (IOException e) {
                    logger.error("Failed to delete attachment {}", path, e);
                }
            }
        }

        announcementRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public AnnouncementEntity findById(Long id) {
        return announcementRepository.findById(id).orElse(null);
    }

    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }

        String originalFileName = cleanOriginalFilename(file);
        UploadFileValidator.validateAttachment(file, originalFileName, ALLOWED_EXTENSIONS);
        String extension = UploadFileValidator.extensionOf(originalFileName);

        String storedFileName = UUID.randomUUID() + "." + extension;

        Path targetLocation = rootFileLocation.resolve(storedFileName).normalize();
        if (!targetLocation.startsWith(rootFileLocation)) {
            throw new IOException("Invalid upload path.");
        }

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        logger.info("Stored attachment {} as {}", originalFileName, storedFileName);
        return storedFileName;
    }

    public String storeSummernoteImage(MultipartFile multipartFile) throws IOException {
        String storedFileName = storeFile(multipartFile);
        return "/uploads/" + storedFileName;
    }

    private void removeDeletedAttachments(List<String> currentAttachmentPaths,
                                          List<String> currentOriginalNames,
                                          List<String> deletedAttachmentPaths) throws IOException {
        List<Integer> indicesToRemove = new ArrayList<>();

        for (String pathToDelete : deletedAttachmentPaths) {
            for (int i = 0; i < currentAttachmentPaths.size(); i++) {
                if (pathToDelete.equals(currentAttachmentPaths.get(i))) {
                    indicesToRemove.add(i);
                    break;
                }
            }
        }

        Collections.sort(indicesToRemove, Collections.reverseOrder());
        for (int index : indicesToRemove) {
            if (index < currentAttachmentPaths.size()) {
                deleteFileFromServer(currentAttachmentPaths.get(index));
                currentAttachmentPaths.remove(index);
            }
            if (index < currentOriginalNames.size()) {
                currentOriginalNames.remove(index);
            }
        }
    }

    private void deleteFileFromServer(String filePathWithContext) throws IOException {
        if (filePathWithContext == null || !filePathWithContext.startsWith("/uploads/")) {
            logger.warn("Invalid file path for deletion: {}", filePathWithContext);
            return;
        }

        String fileName = filePathWithContext.substring("/uploads/".length());
        Path targetLocation = rootFileLocation.resolve(fileName).normalize();
        if (!targetLocation.startsWith(rootFileLocation)) {
            throw new IOException("Invalid file deletion path.");
        }

        Files.deleteIfExists(targetLocation);
    }

    private String cleanOriginalFilename(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (originalFileName.isBlank()) {
            return "unknown_file_" + UUID.randomUUID();
        }
        if (originalFileName.contains("..")) {
            throw new IllegalArgumentException("잘못된 파일 이름입니다.");
        }
        return originalFileName;
    }

    public String sanitizeHtml(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        return Jsoup.clean(content, ANNOUNCEMENT_HTML_POLICY);
    }
}
