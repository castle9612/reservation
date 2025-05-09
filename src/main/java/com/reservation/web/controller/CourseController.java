package com.reservation.web.controller;

import com.reservation.web.dto.CourseDTO;
import com.reservation.web.entity.CourseEntity;
import com.reservation.web.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    // 코스 목록 조회
    @GetMapping
    public String getCourses(Model model) {
        List<CourseEntity> courses = courseService.findAllCourses();
        model.addAttribute("courses", courses);
        return "course/courseList"; // courseList.html로 반환
    }

    // 코스 상세 조회
    @GetMapping("/{courseId}")
    public String getCourseById(@PathVariable String courseId, Model model) {
        Optional<CourseEntity> course = courseService.findCourseById(courseId);
        if (course.isPresent()) {
            model.addAttribute("course", course.get());
            return "course/courseDetail"; // courseDetail.html로 반환
        }
        return "redirect:/courses"; // 코스가 없을 시 목록으로 리다이렉트
    }

    // 코스 등록 페이지로 이동
    @GetMapping("/new")
    public String newCourseForm(Model model) {
        model.addAttribute("courseDTO", new CourseDTO());
        return "course/courseForm"; // courseForm.html로 반환
    }

    // 코스 등록 처리
    @PostMapping
    public String saveCourse(@ModelAttribute CourseDTO courseDTO) {
        courseService.saveCourse(courseDTO);
        return "redirect:/courses"; // 저장 후 코스 목록으로 리다이렉트
    }

    // 코스 수정 페이지로 이동
    @GetMapping("/edit/{courseId}")
    public String editCourseForm(@PathVariable String courseId, Model model) {
        Optional<CourseEntity> course = courseService.findCourseById(courseId);
        if (course.isPresent()) {
            CourseDTO courseDTO = new CourseDTO();
            courseDTO.setId(course.get().getId());
            courseDTO.setName(course.get().getName());
            courseDTO.setStaff(course.get().getStaff());
            courseDTO.setCourseDateTime(course.get().getCourseDateTime().toString());
            courseDTO.setMemberPrice(course.get().getMemberPrice());
            courseDTO.setNonMemberPrice(course.get().getNonMemberPrice());

            model.addAttribute("courseDTO", courseDTO);
            return "course/courseForm"; // 수정 폼을 동일한 폼으로 사용
        }
        return "redirect:/courses"; // 코스가 없을 시 목록으로 리다이렉트
    }

    // 코스 수정 처리
    @PostMapping("/edit/{courseId}")
    public String updateCourse(@PathVariable String courseId, @ModelAttribute CourseDTO courseDTO) {
        courseService.updateCourse(courseId, courseDTO);
        return "redirect:/courses"; // 수정 후 코스 목록으로 리다이렉트
    }

    // 코스 삭제 처리
    @PostMapping("/delete/{courseId}")
    public String deleteCourse(@PathVariable String courseId) {
        courseService.deleteCourse(courseId);
        return "redirect:/courses"; // 삭제 후 코스 목록으로 리다이렉트
    }
}
