package com.reservation.web.controller.api;

import com.reservation.web.dto.CourseDTO;
import com.reservation.web.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class ApiCourseController {

    private final CourseService courseService;

    @GetMapping
    public ApiResponse<List<CourseDTO>> getCourses() {
        List<CourseDTO> courses = courseService.findAllCoursesWithStaff()
                .stream()
                .map(courseService::convertEntityToDTO)
                .toList();
        return ApiResponse.ok(courses);
    }

    @GetMapping("/{courseId}")
    public ApiResponse<CourseDTO> getCourse(@PathVariable Long courseId) {
        CourseDTO dto = courseService.findCourseById(courseId)
                .map(courseService::convertEntityToDTO)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스를 찾을 수 없습니다."));
        return ApiResponse.ok(dto);
    }
}
