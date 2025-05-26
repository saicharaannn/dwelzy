package com.example.oauth2authservice;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;
import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/api/user")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            // You can customize what information to return.
            // For example, return name, email, and authorities.
            return Map.of(
                "name", principal.getAttribute("name"),
                "email", principal.getAttribute("email"),
                "attributes", principal.getAttributes()
            );
        }
        // Should not happen if security is configured correctly and this endpoint is protected
        return Collections.singletonMap("error", "User not authenticated");
    }

    @GetMapping("/")
    public Map<String, String> home() {
        return Collections.singletonMap("message", "Welcome! This is the public home page. Navigate to /api/user to see user info (requires login).");
    }
}
