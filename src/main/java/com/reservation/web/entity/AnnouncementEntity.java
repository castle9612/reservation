package com.reservation.web.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "announcements")
public class AnnouncementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    // 서버에 저장된 파일의 웹 접근 경로 (예: /uploads/uuid.png)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "announcement_attachments",
            joinColumns = @JoinColumn(name = "announcement_id"),
            // 경로 순서를 유지하기 위해 OrderColumn 추가 (선택적이지만 권장)
            foreignKey = @ForeignKey(name = "fk_announcement_attachments_announcement_id"))
    @OrderColumn(name = "attachment_order") // 리스트 순서 유지를 위해
    @Column(name = "file_path")
    private List<String> attachmentPaths = new ArrayList<>();

    // 사용자가 업로드한 파일의 원본 이름. attachmentPaths와 순서가 일치해야 함.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "announcement_original_names",
            joinColumns = @JoinColumn(name = "announcement_id"),
            foreignKey = @ForeignKey(name = "fk_announcement_original_names_announcement_id"))
    @OrderColumn(name = "original_name_order") // 리스트 순서 유지를 위해
    @Column(name = "original_file_name")
    private List<String> originalAttachmentNames = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}