package com.reservation.web.service;

import com.reservation.web.dto.CourseDTO;
import com.reservation.web.dto.StaffDTO;
import com.reservation.web.entity.CourseEntity;
import com.reservation.web.entity.StaffEntity;
import com.reservation.web.repository.CourseRepository;
import com.reservation.web.repository.ReservationRepository;
import com.reservation.web.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final StaffRepository staffRepository;
    private final ReservationRepository reservationRepository;

    public CourseDTO convertEntityToDTO(CourseEntity courseEntity) {
        CourseDTO dto = new CourseDTO();
        dto.setId(courseEntity.getId());
        dto.setName(courseEntity.getName());
        dto.setDurationMinutes(courseEntity.getDurationMinutes());
        dto.setMemberPrice(courseEntity.getMemberPrice());
        dto.setNonMemberPrice(courseEntity.getNonMemberPrice());
        dto.setDisplayOrder(courseEntity.getDisplayOrder() == null ? 0 : courseEntity.getDisplayOrder());

        if (courseEntity.getStaff() != null) {
            StaffEntity staff = courseEntity.getStaff();
            dto.setStaffId(staff.getId());
            dto.setStaff(new StaffDTO(
                    staff.getId(),
                    staff.getName(),
                    staff.getProfilePicture(),
                    staff.getDescription()
            ));
        }

        return dto;
    }

    @Transactional
    public CourseEntity saveCourse(CourseDTO courseDTO) {
        CourseEntity courseEntity = new CourseEntity();
        mapDtoToEntity(courseDTO, courseEntity);
        int nextDisplayOrder = courseRepository.findFirstByOrderByDisplayOrderDescIdDesc()
                .map(course -> course.getDisplayOrder() == null ? 1 : course.getDisplayOrder() + 1)
                .orElse(1);
        courseEntity.setDisplayOrder(nextDisplayOrder);
        return courseRepository.save(courseEntity);
    }

    @Transactional(readOnly = true)
    public List<CourseEntity> findAllCoursesWithStaff() {
        return courseRepository.findAllWithStaffOrdered();
    }

    @Transactional(readOnly = true)
    public Optional<CourseEntity> findCourseById(Long courseId) {
        return courseRepository.findByIdWithStaff(courseId);
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
    public void moveCourse(Long courseId, String direction) {
        List<CourseEntity> courses = courseRepository.findAllByOrderByDisplayOrderAscIdAsc();
        for (int i = 0; i < courses.size(); i++) {
            CourseEntity course = courses.get(i);
            if (course.getDisplayOrder() == null || course.getDisplayOrder() == 0) {
                course.setDisplayOrder(i + 1);
            }
        }

        int currentIndex = -1;
        for (int i = 0; i < courses.size(); i++) {
            if (courseId.equals(courses.get(i).getId())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex < 0) {
            throw new IllegalArgumentException("순서를 변경할 프로그램을 찾을 수 없습니다. ID: " + courseId);
        }

        int targetIndex = "down".equalsIgnoreCase(direction) ? currentIndex + 1 : currentIndex - 1;
        if (targetIndex < 0 || targetIndex >= courses.size()) {
            courseRepository.saveAll(courses);
            return;
        }

        CourseEntity current = courses.get(currentIndex);
        CourseEntity target = courses.get(targetIndex);
        Integer currentOrder = current.getDisplayOrder();
        current.setDisplayOrder(target.getDisplayOrder());
        target.setDisplayOrder(currentOrder);
        courseRepository.saveAll(List.of(current, target));
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("삭제할 코스를 찾을 수 없습니다. ID: " + courseId);
        }
        reservationRepository.deleteByCourse_Id(courseId);
        courseRepository.deleteById(courseId);
    }
}
