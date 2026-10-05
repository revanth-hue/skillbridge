package com.skillbridge.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.skillbridge.entity.User;
import com.skillbridge.service.CurrentUserService;
import com.skillbridge.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class SettingsController {

    private final CurrentUserService currentUserService;
    private final UserService userService;

    public SettingsController(
            CurrentUserService currentUserService,
            UserService userService) {
        this.currentUserService = currentUserService;
        this.userService = userService;
    }

    @GetMapping("/settings")
    public String showSettings(HttpSession session, Model model) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        model.addAttribute("currentUser", user.get());
        return "settings";
    }

    @PostMapping("/settings")
    public String updateSettings(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        try {
            userService.updateProfile(user.get(), fullName, email, bio,
                    currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Settings saved.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/settings";
    }
}