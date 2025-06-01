package com.reservation.web.repository;

import com.reservation.web.entity.AnnouncementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // List 임포트 추가

@Repository
public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, Long> {

    // createdAt 필드를 기준으로 내림차순 정렬하여 모든 공지사항을 찾는 메소드
    List<AnnouncementEntity> findAllByOrderByCreatedAtDesc(); // 이 메소드 추가

}