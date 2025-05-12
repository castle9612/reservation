package com.reservation.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

@Controller
public class TestController {

    // 현재 활성화된 프로파일을 확인하기 위해 Environment 주입 (선택 사항)
    private final Environment environment;

    @Value("${spring.profiles.active:default}") // application.properties/yml 에서 활성 프로파일 가져오기
    private String activeProfile;


    public TestController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/test")
    public String showTestPage(Model model) {
        // 개발 환경에서만 이 페이지에 접근하도록 제한하는 것이 좋습니다.
        // 방법 1: 프로파일 확인 (application.properties/yml에 spring.profiles.active=dev 설정 시)
        boolean isDevelopment = "dev".equals(activeProfile) || Arrays.asList(environment.getActiveProfiles()).contains("dev");

        if (!isDevelopment) {
            // 개발 환경이 아니면 접근 금지 또는 404 처리
            // return "error/404"; // 또는 "redirect:/";
            // 혹은 이 컨트롤러 자체를 dev 프로파일에서만 활성화 (클래스에 @Profile("dev") 어노테이션)
            System.out.println("Warning: Test page accessed in non-development environment. Active profiles: " + Arrays.toString(environment.getActiveProfiles()) + ", spring.profiles.active: " + activeProfile);
            // 운영 환경에서는 이 페이지가 노출되지 않도록 주의해야 합니다.
        }

        model.addAttribute("pageTitle", "기능 테스트 페이지"); // 레이아웃에 페이지 제목 전달
        return "test/test_page"; // src/main/resources/templates/test/test_page.html
    }
}