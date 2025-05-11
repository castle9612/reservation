package com.reservation.web.controller;

import com.reservation.web.dto.CourseDTO;
import com.reservation.web.dto.StaffDTO; // StaffDTO import 추가
import com.reservation.web.entity.CourseEntity;
import com.reservation.web.service.CourseService;
import com.reservation.web.service.StaffService; // StaffService import 추가
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final StaffService staffService; // StaffService 필드 선언 및 주입

    // 코스 목록 조회
    @GetMapping
    public String getCourses(Model model) {
        List<CourseEntity> courses = courseService.findAllCoursesWithStaff();
        model.addAttribute("courses", courses);
        return "course/courseList";
    }

    // 코스 상세 조회
    @GetMapping("/{courseId}")
    public String getCourseById(@PathVariable Long courseId, Model model) {
        return courseService.findCourseById(courseId) // findByIdWithStaff 사용 고려
                .map(course -> {
                    model.addAttribute("course", course);
                    return "course/courseDetail";
                })
                .orElse("redirect:/courses");
    }

    // 코스 등록 폼
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newCourseForm(Model model) {
        model.addAttribute("courseDTO", new CourseDTO());
        // **** 스태프 목록을 조회하여 모델에 'allStaff'라는 이름으로 추가 ****
        List<StaffDTO> staffList = staffService.findAllStaff();
        model.addAttribute("allStaff", staffList); // 템플릿에서 사용하는 이름(${allStaff})과 일치시킴
        System.out.println("CourseController - newCourseForm: Fetched allStaff, size: " + staffList.size());
        return "course/courseForm";
    }

    // 코스 등록 처리
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String saveCourse(@ModelAttribute CourseDTO courseDTO) {
        courseService.saveCourse(courseDTO);
        return "redirect:/courses";
    }

    // 코스 수정 폼
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{courseId}")
    public String editCourseForm(@PathVariable Long courseId, Model model) {
        return courseService.findCourseById(courseId) // findByIdWithStaff 사용 고려
                .map(course -> {
                    CourseDTO dto = courseService.convertEntityToDTO(course);
                    model.addAttribute("courseDTO", dto);
                    // **** 스태프 목록을 조회하여 모델에 'allStaff'라는 이름으로 추가 ****
                    List<StaffDTO> staffList = staffService.findAllStaff();
                    model.addAttribute("allStaff", staffList); // 템플릿에서 사용하는 이름(${allStaff})과 일치시킴
                    System.out.println("CourseController - editCourseForm: Fetched allStaff, size: " + staffList.size());
                    return "course/courseForm";
                })
                .orElse("redirect:/courses");
    }

    // 코스 수정 처리
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/edit/{courseId}")
    public String updateCourse(@PathVariable Long courseId, @ModelAttribute CourseDTO courseDTO) {
        courseService.updateCourse(courseId, courseDTO);
        return "redirect:/courses";
    }

    // 코스 삭제 처리
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{courseId}")
    public String deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return "redirect:/courses";
    }
}