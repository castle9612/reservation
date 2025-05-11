package com.reservation.web.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor; // 기본 생성자 추가
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor // JPA 엔티티는 기본 생성자가 필요합니다.
public class StaffEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // 이름은 필수 항목으로 가정
    private String name;

    @Column // 프로필 사진 경로는 선택 사항일 수 있음
    private String profilePicture; // 사진 경로

    // Staff가 담당하는 Course 목록 (양방향 관계)
    @OneToMany(mappedBy = "staff", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<CourseEntity> courses = new ArrayList<>();

    // 모든 필드를 받는 생성자 (선택적)
    public StaffEntity(String name, String profilePicture) {
        this.name = name;
        this.profilePicture = profilePicture;
    }

    @Override
    public String toString() {
        return name; // Thymeleaf 등에서 객체 자체를 문자열로 표현할 때 이름이 유용
    }
}