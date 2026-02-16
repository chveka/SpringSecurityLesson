package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AuthResponse;
import org.example.dto.LoginRequest;
import org.example.dto.RegisterRequest;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.utils.JWTUtils;
import org.example.utils.Role;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthenticationService {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 24 * 60 * 60 * 1000;
    private final UserRepository userRepository;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .isAccountNonLocked(true)
                .failedAttempts(0)
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        String token = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

        return new AuthResponse(token, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isAccountNonLocked()) {
            if (user.getLockTime() != null &&
                    user.getLockTime().plusMonths(LOCK_TIME_DURATION).isBefore(LocalDateTime.now())) {
                unlockAccount(user.getUsername());
                log.info("Account automatically unlocked: {}", user.getUsername());
            } else {
                throw new RuntimeException("Account is locked. Please try again later.");
            }
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            resetFailedAttempts(user.getUsername());

            String token = jwtUtils.generateToken(user);
            String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

            log.info("User logged in successfully: {}", user.getUsername());
            return new AuthResponse(token, refreshToken);

        } catch (BadCredentialsException e) {
            increaseFailedAttempts(user);
            throw new RuntimeException("Invalid username or password");
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtils.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (jwtUtils.isTokenValid(refreshToken, user)) {
            String newToken = jwtUtils.generateToken(user);
            String newRefreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
            return new AuthResponse(newToken, newRefreshToken);
        }

        throw new RuntimeException("Invalid refresh token");
    }

    private void increaseFailedAttempts(User user) {
        int newFailAttempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(newFailAttempts);

        if (newFailAttempts >= MAX_FAILED_ATTEMPTS) {
            lockAccount(user);
            log.warn("Account locked due to {} failed attempts: {}", MAX_FAILED_ATTEMPTS, user.getUsername());
        }

        userRepository.save(user);
    }

    private void resetFailedAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedAttempts(0);
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            userRepository.save(user);
        });
    }

    private void lockAccount(User user) {
        user.setAccountNonLocked(false);
        user.setLockTime(LocalDateTime.now());
        userRepository.save(user);
    }

    public void unlockAccount(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setAccountNonLocked(true);
            user.setFailedAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
            log.info("Account unlocked: {}", username);
        });
    }
}