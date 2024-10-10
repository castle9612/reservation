package com.reservation.web.service;

import com.reservation.web.dto.CourseDTO;
import com.reservation.web.entity.CourseEntity;
import com.reservation.web.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    // 코스 등록
    public CourseEntity saveCourse(CourseDTO courseDTO) {
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setName(courseDTO.getName());
        courseEntity.setStaff(courseDTO.getStaff());
        courseEntity.setCourseDateTime(LocalDateTime.parse(courseDTO.getCourseDateTime()));
        courseEntity.setMemberPrice(courseDTO.getMemberPrice());
        courseEntity.setNonMemberPrice(courseDTO.getNonMemberPrice());

        return courseRepository.save(courseEntity);
    }

    // 모든 코스 조회
    public List<CourseEntity> findAllCourses() {
        return courseRepository.findAll();
    }

    // 특정 코스 조회
    public Optional<CourseEntity> findCourseById(Long courseId) {
        return courseRepository.findById(courseId);
    }

    // 코스 수정
    public CourseEntity updateCourse(Long courseId, CourseDTO courseDTO) {
        Optional<CourseEntity> optionalCourse = courseRepository.findById(courseId);

        if (optionalCourse.isPresent()) {
            CourseEntity courseEntity = optionalCourse.get();
            courseEntity.setName(courseDTO.getName());
            courseEntity.setStaff(courseDTO.getStaff());
            courseEntity.setCourseDateTime(LocalDateTime.parse(courseDTO.getCourseDateTime()));
            courseEntity.setMemberPrice(courseDTO.getMemberPrice());
            courseEntity.setNonMemberPrice(courseDTO.getNonMemberPrice());

            return courseRepository.save(courseEntity);
        }

        return null;  // 코스가 없을 경우 null 반환
    }

    // 코스 삭제
    public void deleteCourse(Long courseId) {
        courseRepository.deleteById(courseId);
    }

    public List<CourseEntity> findAll() {
        return courseRepository.findAll();  // JpaRepository가 기본적으로 제공
    }
}
