package com.reservation.web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "site_content")
@Getter
@Setter
@NoArgsConstructor
public class SiteContentEntity {

    @Id
    private Long id = 1L;

    @Column(length = 120, nullable = false)
    private String brandName = "라본 바디 테라피";

    @Column(length = 120, nullable = false)
    private String heroEyebrow = "웰니스 스테이";

    @Column(length = 160, nullable = false)
    private String heroTitle = "편안한 회복, 가벼운 시작";

    @Lob
    @Column(columnDefinition = "TEXT")
    private String heroDescription = "차분한 테라피 시간으로 몸의 긴장을 내려놓고, 다시 일상을 시작할 수 있도록 돕습니다.";

    @Column(length = 500)
    private String heroImagePath = "";

    @Column(length = 120, nullable = false)
    private String storeName = "라본 바디 테라피";

    @Column(length = 500, nullable = false)
    private String storeAddress = "예약 전 위치와 이동 동선을 먼저 확인해 두시면 보다 편안하게 방문하실 수 있습니다.";

    @Column(length = 60)
    private String storePhone = "";

    @Lob
    @Column(columnDefinition = "TEXT")
    private String locationDescription = "차분한 테라피 시간으로 이어질 수 있도록 매장 위치를 한눈에 확인하실 수 있게 준비했습니다.";

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
