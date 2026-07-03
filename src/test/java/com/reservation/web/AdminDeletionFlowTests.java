package com.reservation.web;

import com.reservation.web.entity.CourseEntity;
import com.reservation.web.entity.ReservationEntity;
import com.reservation.web.entity.StaffEntity;
import com.reservation.web.repository.CourseRepository;
import com.reservation.web.repository.ReservationRepository;
import com.reservation.web.repository.StaffRepository;
import com.reservation.web.service.CourseService;
import com.reservation.web.service.StaffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AdminDeletionFlowTests {

    @Autowired
    private CourseService courseService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void deletingCourseAlsoDeletesReservationsUsingThatCourse() {
        StaffEntity staff = staffRepository.save(new StaffEntity("담당자", null));
        CourseEntity course = courseRepository.save(new CourseEntity("삭제 테스트 코스", staff, 60, 50000.0, 70000.0));

        ReservationEntity reservation = new ReservationEntity();
        reservation.setCourse(course);
        reservation.setReservationDateTime(LocalDateTime.now().plusDays(3));
        reservation.setStatus("PENDING");
        reservation.setName("비회원");
        reservation.setPhoneNumber("01012345678");
        reservationRepository.save(reservation);

        courseService.deleteCourse(course.getId());

        assertThat(courseRepository.existsById(course.getId())).isFalse();
        assertThat(reservationRepository.findByCourse_Id(course.getId())).isEmpty();
    }

    @Test
    void deletingStaffDetachesCoursesBeforeRemovingStaff() {
        StaffEntity staff = staffRepository.save(new StaffEntity("삭제 대상", null));
        CourseEntity course = courseRepository.save(new CourseEntity("담당자 해제 코스", staff, 45, 40000.0, 60000.0));

        staffService.deleteStaff(staff.getId());

        CourseEntity reloadedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(staffRepository.existsById(staff.getId())).isFalse();
        assertThat(reloadedCourse.getStaff()).isNull();
    }
}
