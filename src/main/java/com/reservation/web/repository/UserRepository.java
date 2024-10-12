package com.reservation.web.repository;

import com.reservation.web.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    // 사용자 ID로 사용자 정보를 조회하는 메소드
    Optional<UserEntity> findByUserId(String userId);

    // 사용자 이름으로 사용자 정보를 조회하는 메소드
    Optional<UserEntity> findByName(String name);
}
