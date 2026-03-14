package com.reservation.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/signup"),
                                new AntPathRequestMatcher("/admin/announcements/uploadSummernoteImageFile")
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/index",
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/favicon.ico", "/uploads/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/auth/csrf",
                                "/api/auth/me",
                                "/api/courses",
                                "/api/courses/*",
                                "/api/reservations/search"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/api/reservations/guest"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/reservations/member").authenticated()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("userId")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(200);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":true,\"message\":\"로그인되었습니다.\"}");
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"아이디 또는 비밀번호가 올바르지 않습니다.\"}");
                        })
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/api/auth/logout"))
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(200);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":true,\"message\":\"로그아웃되었습니다.\"}");
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"로그인이 필요합니다.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"접근 권한이 없습니다.\"}");
                        })
                );

        return http.build();
    }
}