package org.example.repository;

import org.example.entity.Oauth2User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface Oauth2UserRepository extends JpaRepository<Oauth2User, Long> {
    Optional<Oauth2User> findByEmail(String email);
    Optional<Oauth2User> findByProviderId(String providerId);
}