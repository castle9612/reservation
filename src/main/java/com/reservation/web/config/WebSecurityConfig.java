package com.reservation.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer; // 추가
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private CustomAuthFailureHandler customAuthFailureHandler;

    // 🔽 정적 리소스들은 Spring Security 필터링을 무시하도록 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                new AntPathRequestMatcher("/css/**"),
                new AntPathRequestMatcher("/js/**"),
                new AntPathRequestMatcher("/images/**"),
                new AntPathRequestMatcher("/webjars/**"), // ⬅️ WebJars 경로 추가
                new AntPathRequestMatcher("/favicon.ico"),
                new AntPathRequestMatcher("/uploads/**") // 업로드 파일 경로도 여기에 포함
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/signup") // 예: 회원가입 POST는 CSRF 예외
                                // 필요에 따라 다른 API 엔드포인트도 CSRF 예외 처리
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // ⬆️ WebSecurityCustomizer로 옮겼으므로 여기서는 정적 리소스 permitAll 제거
                        .requestMatchers(
                                "/", "/index", "/login", "/signup",
                                "/announcement/list", "/announcement/detail/**", // 상세 페이지는 ID가 붙으므로 /**
                                "/courses", // 코스 안내
                                "/courses/{id}",
                                "/reservations/search", // 비회원 예약 조회
                                "/reservations/new/non-member",
                                "/reservations/save"
                                ).permitAll()
                        .requestMatchers("/reservations").authenticated() // "내 예약"은 인증된 사용자만
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("user_id")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureHandler(customAuthFailureHandler)
                        .permitAll() // 로그인 페이지 자체와 로그인 처리 URL은 모두 접근 가능
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // POST 권장
                        .logoutSuccessUrl("/") // 로그아웃 성공 후 리다이렉트될 페이지
                        .invalidateHttpSession(true) // 세션 무효화
                        .deleteCookies("JSESSIONID", "CSRF-TOKEN") // JSESSIONID 및 CSRF 토큰 쿠키 삭제 (CSRF 쿠키 이름 확인)
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/403")
                );

        return http.build();
    }
}