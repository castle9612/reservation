package com.reservation.web.config;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private CustomAuthFailureHandler customAuthFailureHandler;

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
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/favicon.ico", "/uploads/**"
                        ).permitAll()

                        .requestMatchers(
                                "/", "/index", "/login", "/signup",
                                "/announcement/list", "/announcement/detail/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/courses", "/courses/*").permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/reservations/search",
                                "/reservations/new/non-member"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/reservations/search",
                                "/reservations/save"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/courses/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/courses").hasRole("ADMIN")
                        .requestMatchers("/courses/edit/**", "/courses/delete/**").hasRole("ADMIN")

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/reservations/**", "/mypage/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("user_id")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureHandler(customAuthFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "CSRF-TOKEN")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex.accessDeniedPage("/403"));

        return http.build();
    }
}