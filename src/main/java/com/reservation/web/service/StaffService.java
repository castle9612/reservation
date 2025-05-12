package com.reservation.web.service;

import com.reservation.web.dto.StaffDTO;
import com.reservation.web.entity.StaffEntity;
import com.reservation.web.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 조회만 하는 경우엔 필수는 아님

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;

    // Entity를 DTO로 변환
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

    // StaffService.java
    @Transactional(readOnly = true)
    public List<StaffDTO> findAllStaff() {
        return staffRepository.findAll().stream()
                .map(this::convertEntityToDTO) // convertEntityToDTO는 StaffEntity -> StaffDTO 변환
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

    // Staff 정보 저장/수정 (필요한 경우)
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

    // Staff 삭제 (필요한 경우)
    @Transactional
    public void deleteStaff(Long id) {
        if (!staffRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 직원을 찾을 수 없습니다: " + id);
        }
        // 연관된 Course가 있다면 어떻게 처리할지 정책 필요 (예: null로 만들거나, 삭제 못하게 하거나)
        staffRepository.deleteById(id);
    }
}