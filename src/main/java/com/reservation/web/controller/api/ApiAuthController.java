package com.reservation.web.controller.api;

public class ApiAuthController {
}
package com.reservation.web.controller.api;

import com.reservation.web.dto.UserDTO;
import com.reservation.web.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final UserService userService;

    @GetMapping("/csrf")
    public ApiResponse<CsrfTokenResponse> csrf(CsrfToken csrfToken) {
        return ApiResponse.ok(new CsrfTokenResponse(csrfToken.getToken()));
    }

    @GetMapping("/me")
    public ApiResponse<AuthStatusResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ApiResponse.ok(new AuthStatusResponse(false, null, null));
        }

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        return ApiResponse.ok(new AuthStatusResponse(true, authentication.getName(), role));
    }

    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody UserDTO userDTO) {
        userService.signup(userDTO);
        return ApiResponse.ok(null, "회원가입이 완료되었습니다.");
    }
}