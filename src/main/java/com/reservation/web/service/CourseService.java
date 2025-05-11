package com.reservation.web.service;

import com.reservation.web.dto.CourseDTO;
import com.reservation.web.entity.CourseEntity;
import com.reservation.web.entity.StaffEntity;
import com.reservation.web.repository.CourseRepository;
import com.reservation.web.repository.StaffRepository; // StaffRepository를 직접 사용
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final StaffRepository staffRepository; // Course 생성/수정 시 StaffEntity 조회를 위해 직접 사용

    /**
     * CourseEntity를 CourseDTO로 변환합니다. (수정 폼 데이터 채우기용)
     */
    public CourseDTO convertEntityToDTO(CourseEntity courseEntity) {
        CourseDTO dto = new CourseDTO();
        dto.setId(courseEntity.getId());
        dto.setName(courseEntity.getName());
        if (courseEntity.getStaff() != null) {
            dto.setStaffId(courseEntity.getStaff().getId());
        }
        dto.setCourseDateTime(courseEntity.getCourseDateTime());
        dto.setDurationMinutes(courseEntity.getDurationMinutes());
        dto.setMemberPrice(courseEntity.getMemberPrice());
        dto.setNonMemberPrice(courseEntity.getNonMemberPrice());
        return dto;
    }

    @Transactional
    public CourseEntity saveCourse(CourseDTO courseDTO) {
        CourseEntity courseEntity = new CourseEntity();
        // DTO -> Entity 변환 로직 (공통화 가능)
        mapDtoToEntity(courseDTO, courseEntity);
        return courseRepository.save(courseEntity);
    }

    @Transactional(readOnly = true)
    public List<CourseEntity> findAllCoursesWithStaff() {
        return courseRepository.findAllWithStaff();
    }

    @Transactional(readOnly = true)
    public Optional<CourseEntity> findCourseById(Long courseId) {
        // Staff 정보까지 한번에 가져오려면 CourseRepository에 findByIdWithStaff 같은 메소드 정의 후 사용
        return courseRepository.findById(courseId);
    }

    @Transactional
    public CourseEntity updateCourse(Long courseId, CourseDTO courseDTO) {
        CourseEntity courseEntity = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 코스를 찾을 수 없습니다. ID: " + courseId));
        // DTO -> Entity 변환 로직
        mapDtoToEntity(courseDTO, courseEntity);
        return courseRepository.save(courseEntity);
    }

    // DTO의 정보로 Entity를 업데이트하는 헬퍼 메소드
    private void mapDtoToEntity(CourseDTO courseDTO, CourseEntity courseEntity) {
        courseEntity.setName(courseDTO.getName());

        if (courseDTO.getStaffId() != null) {
            StaffEntity staff = staffRepository.findById(courseDTO.getStaffId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 ID의 직원을 찾을 수 없습니다: " + courseDTO.getStaffId()));
            courseEntity.setStaff(staff);
        } else {
            // 직원 미지정 또는 필수 여부에 따른 처리
            // 예: throw new IllegalArgumentException("담당자 지정은 필수입니다.");
            courseEntity.setStaff(null); // 직원을 미지정으로 설정 허용
        }

        courseEntity.setCourseDateTime(courseDTO.getCourseDateTime());
        courseEntity.setDurationMinutes(courseDTO.getDurationMinutes());
        courseEntity.setMemberPrice(courseDTO.getMemberPrice());
        courseEntity.setNonMemberPrice(courseDTO.getNonMemberPrice());
    }


    // StaffEntity를 직접 받는 오버로드된 updateCourse (필요시 사용)
    @Transactional
    public void updateCourse(Long courseId, CourseDTO courseDTO, StaffEntity staff) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스를 찾을 수 없습니다. ID: " + courseId));

        course.setName(courseDTO.getName());
        course.setStaff(staff); // 직접 받은 StaffEntity 사용
        course.setCourseDateTime(courseDTO.getCourseDateTime());
        course.setDurationMinutes(courseDTO.getDurationMinutes());
        course.setMemberPrice(courseDTO.getMemberPrice());
        course.setNonMemberPrice(courseDTO.getNonMemberPrice());

        courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("삭제할 코스를 찾을 수 없습니다. ID: " + courseId);
        }
        courseRepository.deleteById(courseId);
    }
}