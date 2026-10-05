package com.skillbridge.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.skillbridge.entity.User;
import com.skillbridge.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(User user) {
        validateProfile(user.getFullName(), user.getEmail(), user.getBio());
        user.setFullName(user.getFullName().trim());
        user.setEmail(normalizeEmail(user.getEmail()));

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        validatePassword(user.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public Optional<User> loginUser(String email, String password) {

        Optional<User> user = userRepository.findByEmail(normalizeEmail(email));

        if (user.isPresent()) {
            String storedPassword = user.get().getPassword();
            boolean isHashed = storedPassword.startsWith("$2");
            boolean isValid = isHashed
                    ? passwordEncoder.matches(password, storedPassword)
                    : storedPassword.equals(password);

            if (isValid) {
                if (!isHashed) {
                    user.get().setPassword(passwordEncoder.encode(password));
                    userRepository.save(user.get());
                }
                return user;
            }
        }

        return Optional.empty();
    }

    public User updateProfile(
            User user,
            String fullName,
            String email,
            String bio,
            String currentPassword,
            String newPassword) {
        validateProfile(fullName, email, bio);
        String normalizedEmail = normalizeEmail(email);
        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);
        if (existingUser.isPresent()
                && !existingUser.get().getId().equals(user.getId())) {
            throw new RuntimeException("Email already registered");
        }

        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setBio(bio == null || bio.isBlank() ? null : bio.trim());

        if (newPassword != null && !newPassword.isBlank()) {
            validatePassword(newPassword);
            String storedPassword = user.getPassword();
            boolean currentPasswordMatches = currentPassword != null
                && storedPassword != null
                && (storedPassword.startsWith("$2")
                    ? passwordEncoder.matches(currentPassword, storedPassword)
                    : storedPassword.equals(currentPassword));
            if (!currentPasswordMatches) {
                throw new RuntimeException("Current password is incorrect.");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        return userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required.");
        }
        String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalizedEmail.length() > 254
                || !normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new RuntimeException("Enter a valid email address.");
        }
        return normalizedEmail;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("Password must contain at least 8 characters.");
        }
        if (password.length() > 72) {
            throw new RuntimeException("Password must contain 72 characters or fewer.");
        }
    }

    private void validateProfile(String fullName, String email, String bio) {
        if (fullName == null || fullName.isBlank() || fullName.trim().length() > 120) {
            throw new RuntimeException("Enter a name of 120 characters or fewer.");
        }
        if (bio != null && bio.length() > 500) {
            throw new RuntimeException("Bio must be 500 characters or fewer.");
        }
        normalizeEmail(email);
    }
}