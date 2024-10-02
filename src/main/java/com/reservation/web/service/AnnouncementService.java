package com.reservation.web.service;

import com.reservation.web.entity.AnnouncementEntity;
import com.reservation.web.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public List<AnnouncementEntity> findAll() {
        return announcementRepository.findAll();
    }

    public AnnouncementEntity save(AnnouncementEntity announcement) {
        return announcementRepository.save(announcement);
    }
}
