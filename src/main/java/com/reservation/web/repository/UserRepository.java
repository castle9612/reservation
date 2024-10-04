package com.reservation.web.repository;

import com.reservation.web.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // <객체 type, pk type>
public interface UserRepository extends JpaRepository<UserEntity, String> {

}