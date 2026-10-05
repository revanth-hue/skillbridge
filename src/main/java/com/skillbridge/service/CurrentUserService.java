package com.skillbridge.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.skillbridge.entity.User;
import com.skillbridge.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findCurrentUser(HttpSession session) {
        Object userId = session.getAttribute("userId");
        if (!(userId instanceof Long id)) {
            return Optional.empty();
        }

        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            session.invalidate();
        }
        return user;
    }
}