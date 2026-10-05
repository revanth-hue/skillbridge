package com.skillbridge.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.skillbridge.entity.User;
import com.skillbridge.service.CurrentUserService;
import com.skillbridge.service.SkillMatchingService;

import jakarta.servlet.http.HttpSession;

@Controller
public class SkillMatchingController {

    private final SkillMatchingService skillMatchingService;
    private final CurrentUserService currentUserService;

    public SkillMatchingController(
            SkillMatchingService skillMatchingService,
            CurrentUserService currentUserService) {

        this.skillMatchingService = skillMatchingService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/skill-matching")
    public String showSkillMatchingPage(
            HttpSession session,
            Model model) {

        Optional<User> user = currentUserService.findCurrentUser(session);

        if (user.isEmpty()) {
            return "redirect:/login";
        }

        List<User> matches =
                skillMatchingService.findMatches(user.get());

        model.addAttribute("currentUser", user.get());
        model.addAttribute("matches", matches);

        if (matches.isEmpty()) {
            model.addAttribute("message",
                    "No mutual skill matches found yet.");
        }

        return "skill-matching";
    }
}