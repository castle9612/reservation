package com.reservation.web.repository;

import com.reservation.web.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    // 필요한 경우 추가적인 쿼리 메소드 정의 가능
}
