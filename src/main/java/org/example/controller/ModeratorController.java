package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mod")
@RequiredArgsConstructor
public class ModeratorController {

    @GetMapping("/content")
    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN')")
    public ResponseEntity<String> moderateContent() {
        return ResponseEntity.ok("Content moderation accessed");
    }

    @PutMapping("/content/{id}")
    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN')")
    public ResponseEntity<String> updateContent(@PathVariable Long id) {
        return ResponseEntity.ok("Content updated: " + id);
    }
}