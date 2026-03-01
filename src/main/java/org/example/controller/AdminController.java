package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.User;
import org.example.repository.Oauth2UserRepository;
import org.example.repository.UserRepository;
import org.example.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AuthenticationService authService;
    private final UserRepository userRepository;
    private final Oauth2UserRepository oauth2UserRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/unlock/{username}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> unlockAccount(@PathVariable String username) {
        authService.unlockAccount(username);
        return ResponseEntity.ok("Account unlocked: " + username);
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String adminPanel(Model model) {
        model.addAttribute("users", oauth2UserRepository.findAll());
        return "admin";
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String dashboard() {
        return "admin-dashboard";
    }
}
