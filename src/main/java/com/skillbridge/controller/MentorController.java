package com.skillbridge.controller;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.skillbridge.entity.Skill;
import com.skillbridge.entity.User;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.CurrentUserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MentorController {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public MentorController(
            UserRepository userRepository,
            CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/dashboard")
    public String showMarketplace(
            @RequestParam(required = false) String q,
            HttpSession session,
            Model model) {
        return showMembers(q, session, model, false);
    }

    @GetMapping("/mentors")
    public String showMentorDirectory(
            @RequestParam(required = false) String q,
            HttpSession session,
            Model model) {
        return showMembers(q, session, model, true);
    }

    private String showMembers(
            String q,
            HttpSession session,
            Model model,
            boolean mentorDirectory) {
        Optional<User> currentUser = currentUserService.findCurrentUser(session);
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        List<User> mentors = userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUser.get().getId()))
                .filter(user -> !user.getSkillsToTeach().isEmpty())
                .filter(user -> matchesQuery(user, query))
                .toList();

        model.addAttribute("currentUser", currentUser.get());
        model.addAttribute("mentors", mentors);
        model.addAttribute("query", q == null ? "" : q);
        model.addAttribute("activeModule", mentorDirectory ? "mentors" : "marketplace");
        model.addAttribute("pageTitle", mentorDirectory ? "Mentor Directory" : "Skill Marketplace");
        model.addAttribute("sectionTitle", mentorDirectory ? "People ready to teach" : "Explore skills");
        model.addAttribute("searchPath", mentorDirectory ? "/mentors" : "/dashboard");
        model.addAttribute("viewAllPath", mentorDirectory ? "/dashboard" : "/mentors");
        return "dashboard";
    }

    private boolean matchesQuery(User user, String query) {
        if (query.isBlank()) {
            return true;
        }
        if (contains(user.getFullName(), query) || contains(user.getBio(), query)) {
            return true;
        }
        return user.getSkillsToTeach().stream().map(Skill::getName)
                .anyMatch(name -> contains(name, query));
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}