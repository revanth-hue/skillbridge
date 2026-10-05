package com.skillbridge.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.skillbridge.entity.User;
import com.skillbridge.service.CurrentUserService;
import com.skillbridge.service.SkillExchangeRequestService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LearningSessionController {

    private final CurrentUserService currentUserService;
    private final SkillExchangeRequestService requestService;

    public LearningSessionController(
            CurrentUserService currentUserService,
            SkillExchangeRequestService requestService) {
        this.currentUserService = currentUserService;
        this.requestService = requestService;
    }

    @GetMapping("/sessions")
    public String showSessions(HttpSession session, Model model) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        model.addAttribute("currentUser", user.get());
        model.addAttribute("sessions", requestService.getUserRequests(user.get()));
        return "sessions";
    }

    @PostMapping("/sessions/schedule")
    public String schedule(
            @RequestParam Long requestId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
                    LocalDateTime scheduledAt,
                @RequestParam String googleMeetUrl,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        try {
            requestService.scheduleSession(requestId, user.get(), scheduledAt, googleMeetUrl);
            redirectAttributes.addFlashAttribute("success", "Online class scheduled.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sessions";
    }

    @PostMapping("/sessions/complete")
    public String complete(
            @RequestParam Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        try {
            requestService.completeSession(requestId, user.get());
            redirectAttributes.addFlashAttribute("success", "Session marked complete.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sessions";
    }
}