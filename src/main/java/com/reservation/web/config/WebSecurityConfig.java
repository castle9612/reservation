package com.reservation.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/admin/announcements/uploadSummernoteImageFile"),
                                new AntPathRequestMatcher("/api/auth/signup"),
                                new AntPathRequestMatcher("/signup")
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index",
                                "/login",
                                "/signup",
                                "/test",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/react-app/**",
                                "/uploads/**",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers(
                                "/announcement/**",
                                "/staff",
                                "/staff/",
                                "/staff/*",
                                "/reservations/new/non-member",
                                "/reservations/save",
                                "/reservations/search",
                                "/reservations/search/result"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/auth/csrf",
                                "/api/auth/me",
                                "/api/public/**",
                                "/api/courses",
                                "/api/courses/*",
                                "/api/reservations/search"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/signup",
                                "/api/auth/signup",
                                "/api/reservations/guest"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/staff/new", "/staff/edit/**", "/staff/delete/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reservations/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/reservations/member").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("userId")
                        .passwordParameter("password")
                        .successHandler(this::handleLoginSuccess)
                        .failureHandler((request, response, exception) -> {
                            if (isAjaxRequest(request)) {
                                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, false, "아이디 또는 비밀번호가 올바르지 않습니다.");
                            } else {
                                request.getSession().setAttribute("errorMessage", "아이디 또는 비밀번호가 올바르지 않습니다.");
                                response.sendRedirect("/login?error");
                            }
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new OrRequestMatcher(
                                new AntPathRequestMatcher("/logout", "POST"),
                                new AntPathRequestMatcher("/api/auth/logout", "POST")
                        ))
                        .logoutSuccessHandler((request, response, authentication) -> {
                            if (isAjaxRequest(request)) {
                                writeJson(response, HttpServletResponse.SC_OK, true, "로그아웃되었습니다.");
                            } else {
                                response.sendRedirect("/login?logout");
                            }
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (isAjaxRequest(request) || request.getRequestURI().startsWith("/api/")) {
                                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, false, "로그인이 필요합니다.");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (isAjaxRequest(request) || request.getRequestURI().startsWith("/api/")) {
                                writeJson(response, HttpServletResponse.SC_FORBIDDEN, false, "접근 권한이 없습니다.");
                            } else {
                                response.sendRedirect("/test?denied");
                            }
                        })
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    private void handleLoginSuccess(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Authentication authentication) throws IOException {
        if (isAjaxRequest(request)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("message", "로그인되었습니다.");
            body.put("username", authentication.getName());
            response.getWriter().write(objectMapper.writeValueAsString(body));
        } else {
            response.sendRedirect("/");
        }
    }

    private void writeJson(HttpServletResponse response, int status, boolean success, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("success", success);
        body.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        String contentType = request.getContentType();

        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE))
                || (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE));
    }
}
