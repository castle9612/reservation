package com.reservation.web.repository;

import com.reservation.web.entity.AnnouncementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // List 임포트 추가

@Repository
public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, Long> {

    // createdAt 필드를 기준으로 내림차순 정렬하여 모든 공지사항을 찾는 메소드
    List<AnnouncementEntity> findAllByOrderByCreatedAtDesc(); // 이 메소드 추가

    // 필요하다면 다른 커스텀 쿼리 메소드도 여기에 추가할 수 있습니다.
    // 예: 제목으로 검색 (대소문자 구분 없이, 포함하는 내용)
    // List<AnnouncementEntity> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);
}