package org.example.service;

import lombok.AllArgsConstructor;
import org.example.entity.Oauth2User;
import org.example.repository.Oauth2UserRepository;
import org.example.utils.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@AllArgsConstructor
public class SocialAppService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger logger = LoggerFactory.getLogger(SocialAppService.class);
    private final Oauth2UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oauth2User.getAttribute("id").toString();
        String email = getEmail(oauth2User, provider);
        String name = getName(oauth2User, provider);

        logger.info("Loading user from provider: {}, email: {}", provider, email);

        Oauth2User user = saveOrUpdateUser(provider, providerId, email, name);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        attributes.put("role", user.getRole().name());

        logger.info("User {} authenticated with role: {}", email, user.getRole());

        return new DefaultOAuth2User(
                authorities,
                attributes,
                getMainAttributeKey(provider)
        );
    }

    private Oauth2User saveOrUpdateUser(String provider, String providerId, String email, String name) {
        Optional<Oauth2User> existingUser = userRepository.findByProviderId(providerId);

        Oauth2User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setName(name);
            user.setLastLoginDate(java.time.LocalDateTime.now());
            logger.info("Updating existing user: {}", email);
        } else {
            user = new Oauth2User();
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setEmail(email);
            user.setName(name);
            user.setRole(determineRole(email));
            logger.info("Creating new user: {} with role: {}", email, user.getRole());
        }

        return userRepository.save(user);
    }

    private Role determineRole(String email) {
        if (email != null && email.endsWith("@admin.com")) {
            return Role.SUPER_ADMIN;
        }
        return Role.USER;
    }

    private String getEmail(OAuth2User oauth2User, String provider) {
        if ("github".equals(provider)) {
            return oauth2User.getAttribute("email");
        }
        return oauth2User.getAttribute("email");
    }

    private String getName(OAuth2User oauth2User, String provider) {
        if ("github".equals(provider)) {
            return oauth2User.getAttribute("name") != null ?
                    oauth2User.getAttribute("name") :
                    oauth2User.getAttribute("login");
        }
        return oauth2User.getAttribute("name");
    }

    private String getMainAttributeKey(String provider) {
        if ("github".equals(provider)) {
            return "id";
        }
        return "sub";
    }
}