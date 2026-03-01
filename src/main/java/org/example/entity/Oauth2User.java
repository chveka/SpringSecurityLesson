package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.utils.Role;

import java.time.LocalDateTime;

@Entity
@Table(name = "oauth2Users")
@Data
@NoArgsConstructor
public class Oauth2User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String name;

    @Column(unique = true)
    private String providerId;

    private String provider;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdDate;
    private LocalDateTime lastLoginDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        lastLoginDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastLoginDate = LocalDateTime.now();
    }
}