package com.reservation.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SecurityRateLimitFilter extends OncePerRequestFilter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int LOGIN_LIMIT = 10;
    private static final int SIGNUP_LIMIT = 8;
    private static final int GUEST_LOOKUP_LIMIT = 20;
    private static final Duration WINDOW = Duration.ofMinutes(10);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        LimitRule rule = resolveRule(request);
        if (rule == null || allow(rule, request)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(WINDOW.toSeconds()));

        if (expectsJson(request)) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            OBJECT_MAPPER.writeValue(response.getWriter(), Map.of(
                    "success", false,
                    "message", "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."
            ));
        } else {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getWriter().write("요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    private boolean allow(LimitRule rule, HttpServletRequest request) {
        String key = rule.name + ":" + clientKey(request);
        Instant now = Instant.now();
        Bucket bucket = buckets.compute(key, (ignored, existing) -> {
            if (existing == null || existing.expiresAt.isBefore(now)) {
                return new Bucket(1, now.plus(WINDOW));
            }
            existing.count++;
            return existing;
        });
        cleanupExpiredBuckets(now);
        return bucket.count <= rule.limit;
    }

    private LimitRule resolveRule(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("POST".equalsIgnoreCase(method) && "/login".equals(path)) {
            return new LimitRule("login", LOGIN_LIMIT);
        }
        if ("POST".equalsIgnoreCase(method) && ("/api/auth/signup".equals(path) || "/signup".equals(path))) {
            return new LimitRule("signup", SIGNUP_LIMIT);
        }
        if (("GET".equalsIgnoreCase(method) && "/api/reservations/search".equals(path))
                || ("POST".equalsIgnoreCase(method) && "/reservations/search".equals(path))) {
            return new LimitRule("guest-lookup", GUEST_LOOKUP_LIMIT);
        }
        return null;
    }

    private String clientKey(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        return remoteAddress == null || remoteAddress.isBlank() ? "unknown" : remoteAddress;
    }

    private boolean expectsJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        return request.getRequestURI().startsWith("/api/")
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE));
    }

    private void cleanupExpiredBuckets(Instant now) {
        if (buckets.size() < 1000) {
            return;
        }
        buckets.entrySet().removeIf(entry -> entry.getValue().expiresAt.isBefore(now));
    }

    private record LimitRule(String name, int limit) {
    }

    private static final class Bucket {
        private int count;
        private final Instant expiresAt;

        private Bucket(int count, Instant expiresAt) {
            this.count = count;
            this.expiresAt = expiresAt;
        }
    }
}
