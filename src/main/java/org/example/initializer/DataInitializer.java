package org.example.initializer;

import lombok.RequiredArgsConstructor;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.utils.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.SUPER_ADMIN)
                    .isAccountNonLocked(true)
                    .failedAttempts(0)
                    .build();
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("moderator")) {
            User moderator = User.builder()
                    .username("moderator")
                    .password(passwordEncoder.encode("mod123"))
                    .role(Role.MODERATOR)
                    .isAccountNonLocked(true)
                    .failedAttempts(0)
                    .build();
            userRepository.save(moderator);
        }

        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.USER)
                    .isAccountNonLocked(true)
                    .failedAttempts(0)
                    .build();
            userRepository.save(user);
        }
    }
}