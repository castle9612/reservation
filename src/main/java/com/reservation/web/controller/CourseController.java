package com.reservation.web.controller;

import com.reservation.web.dto.CourseDTO;
import com.reservation.web.dto.StaffDTO;
import com.reservation.web.entity.CourseEntity;
import com.reservation.web.service.CourseService;
import com.reservation.web.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final StaffService staffService;

    @GetMapping
    public String getCourses(Model model) {
        List<CourseDTO> courses = courseService.findAllCoursesWithStaff()
                .stream()
                .map(courseService::convertEntityToDTO)
                .collect(Collectors.toList());
        model.addAttribute("courses", courses);
        return "course/courseList";
    }

    @GetMapping("/{courseId}")
    public String getCourseById(@PathVariable Long courseId, Model model) {
        return courseService.findCourseById(courseId)
                .map(course -> {
                    CourseDTO dto = courseService.convertEntityToDTO(course);
                    model.addAttribute("course", dto);
                    return "course/courseDetail";
                })
                .orElse("redirect:/courses");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newCourseForm(Model model) {
        model.addAttribute("courseDTO", new CourseDTO());
        List<StaffDTO> staffList = staffService.findAllStaff();
        model.addAttribute("allStaff", staffList);
        return "course/courseForm";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String saveCourse(@ModelAttribute CourseDTO courseDTO) {
        courseService.saveCourse(courseDTO);
        return "redirect:/courses";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{courseId}")
    public String editCourseForm(@PathVariable Long courseId, Model model) {
        return courseService.findCourseById(courseId)
                .map(course -> {
                    CourseDTO dto = courseService.convertEntityToDTO(course);
                    model.addAttribute("courseDTO", dto);
                    List<StaffDTO> staffList = staffService.findAllStaff();
                    model.addAttribute("allStaff", staffList);
                    return "course/courseForm";
                })
                .orElse("redirect:/courses");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/edit/{courseId}")
    public String updateCourse(@PathVariable Long courseId, @ModelAttribute CourseDTO courseDTO) {
        courseService.updateCourse(courseId, courseDTO);
        return "redirect:/courses";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{courseId}")
    public String deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return "redirect:/courses";
    }
}
