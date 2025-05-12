package com.reservation.web.service;

import com.reservation.web.dto.CourseDTO;
import com.reservation.web.dto.StaffDTO;
import com.reservation.web.entity.CourseEntity;
import com.reservation.web.entity.StaffEntity;
import com.reservation.web.repository.CourseRepository;
import com.reservation.web.repository.StaffRepository;
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
    private final StaffRepository staffRepository;

    public CourseDTO convertEntityToDTO(CourseEntity courseEntity) {
        CourseDTO dto = new CourseDTO();
        dto.setId(courseEntity.getId());
        dto.setName(courseEntity.getName());
        dto.setDurationMinutes(courseEntity.getDurationMinutes());
        dto.setMemberPrice(courseEntity.getMemberPrice());
        dto.setNonMemberPrice(courseEntity.getNonMemberPrice());

        if (courseEntity.getStaff() != null) {
            StaffEntity staff = courseEntity.getStaff();
            dto.setStaffId(staff.getId());
            dto.setStaff(new StaffDTO(staff.getId(), staff.getName(), staff.getProfilePicture()));
        }

        return dto;
    }

    @Transactional
    public CourseEntity saveCourse(CourseDTO courseDTO) {
        CourseEntity courseEntity = new CourseEntity();
        mapDtoToEntity(courseDTO, courseEntity);
        return courseRepository.save(courseEntity);
    }

    @Transactional(readOnly = true)
    public List<CourseEntity> findAllCoursesWithStaff() {
        return courseRepository.findAllWithStaff();
    }

    @Transactional(readOnly = true)
    public Optional<CourseEntity> findCourseById(Long courseId) {
        return courseRepository.findById(courseId);
    }

    @Transactional
    public CourseEntity updateCourse(Long courseId, CourseDTO courseDTO) {
        CourseEntity courseEntity = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 코스를 찾을 수 없습니다. ID: " + courseId));
        mapDtoToEntity(courseDTO, courseEntity);
        return courseRepository.save(courseEntity);
    }

    private void mapDtoToEntity(CourseDTO courseDTO, CourseEntity courseEntity) {
        courseEntity.setName(courseDTO.getName());

        if (courseDTO.getStaffId() != null) {
            StaffEntity staff = staffRepository.findById(courseDTO.getStaffId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 ID의 직원을 찾을 수 없습니다: " + courseDTO.getStaffId()));
            courseEntity.setStaff(staff);
        } else {
            courseEntity.setStaff(null);
        }

        courseEntity.setDurationMinutes(courseDTO.getDurationMinutes());
        courseEntity.setMemberPrice(courseDTO.getMemberPrice());
        courseEntity.setNonMemberPrice(courseDTO.getNonMemberPrice());
    }

    @Transactional
    public void updateCourse(Long courseId, CourseDTO courseDTO, StaffEntity staff) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스를 찾을 수 없습니다. ID: " + courseId));

        course.setName(courseDTO.getName());
        course.setStaff(staff);
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
