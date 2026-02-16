package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'SUPER_ADMIN')")
    public ResponseEntity<String> getProfile() {
        return ResponseEntity.ok("User profile accessed");
    }

    @GetMapping("/home")
    public String home() {
        return "index";
    }

    @GetMapping("/oauth2user")
    public String user(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) {
            return "redirect:/";
        }

        Map<String, Object> attributes = principal.getAttributes();
        String name = (String) attributes.get("name");
        String login = (String) attributes.get("login");
        String email = (String) attributes.get("email");
        String role = (String) attributes.get("role");

        logger.debug("Displaying user profile for: {}", email);

        model.addAttribute("name", name != null ? name : "Not provided");
        model.addAttribute("login", login != null ? login : "Not provided");
        model.addAttribute("id", attributes.get("id") != null ? attributes.get("id").toString() : "Not provided");
        model.addAttribute("email", email != null ? email : "Not provided");
        model.addAttribute("role", role != null ? role : "USER");
        model.addAttribute("attributes", attributes);
        return "user";
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/";
    }
}