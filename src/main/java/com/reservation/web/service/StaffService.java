package com.reservation.web.service;

import com.reservation.web.dto.StaffDTO;
import com.reservation.web.entity.StaffEntity;
import com.reservation.web.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;

    private StaffDTO convertEntityToDTO(StaffEntity staffEntity) {
        return new StaffDTO(
                staffEntity.getId(),
                staffEntity.getName(),
                staffEntity.getProfilePicture()
        );
    }

    @Transactional(readOnly = true) // 데이터 변경이 없으므로 readOnly
    public List<StaffEntity> findAllStaffEntities() { // 메서드명 명확화
        return staffRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StaffDTO> findAllStaff() {
        return staffRepository.findAll().stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public StaffEntity findEntityById(Long id) { // Entity 반환
        return staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 직원이 존재하지 않습니다: " + id));
    }

    @Transactional(readOnly = true)
    public StaffDTO findById(Long id) { // DTO 반환
        StaffEntity staffEntity = staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 직원이 존재하지 않습니다: " + id));
        return convertEntityToDTO(staffEntity);
    }

    @Transactional
    public StaffEntity saveStaff(StaffDTO staffDTO) {
        StaffEntity staffEntity;
        if (staffDTO.getId() != null) {
            staffEntity = staffRepository.findById(staffDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("수정할 직원을 찾을 수 없습니다: " + staffDTO.getId()));
        } else {
            staffEntity = new StaffEntity();
        }
        staffEntity.setName(staffDTO.getName());
        staffEntity.setProfilePicture(staffDTO.getProfilePicture());
        return staffRepository.save(staffEntity);
    }

    @Transactional
    public void deleteStaff(Long id) {
        if (!staffRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 직원을 찾을 수 없습니다: " + id);
        }
        staffRepository.deleteById(id);
    }
}