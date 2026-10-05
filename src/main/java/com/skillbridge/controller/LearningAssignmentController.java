package com.skillbridge.controller;

import java.time.LocalDate;
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
import com.skillbridge.service.LearningAssignmentService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LearningAssignmentController {

    private final CurrentUserService currentUserService;
    private final LearningAssignmentService assignmentService;

    public LearningAssignmentController(
            CurrentUserService currentUserService,
            LearningAssignmentService assignmentService) {
        this.currentUserService = currentUserService;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/assignments")
    public String showAssignments(HttpSession session, Model model) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        model.addAttribute("currentUser", user.get());
        model.addAttribute("assignments", assignmentService.getAssignments(user.get()));
        return "assignments";
    }

    @PostMapping("/assignments/add")
    public String addAssignment(
            @RequestParam String title,
            @RequestParam(required = false) String details,
            @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        try {
            assignmentService.addAssignment(user.get(), title, details, dueDate);
            redirectAttributes.addFlashAttribute("success", "Assignment added.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/assignments";
    }

    @PostMapping("/assignments/toggle")
    public String toggleAssignment(
            @RequestParam Long assignmentId,
            @RequestParam boolean completed,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        try {
            assignmentService.setCompleted(assignmentId, user.get(), completed);
            redirectAttributes.addFlashAttribute("success", "Assignment updated.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/assignments";
    }

    @PostMapping("/assignments/delete")
    public String deleteAssignment(
            @RequestParam Long assignmentId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        try {
            assignmentService.deleteAssignment(assignmentId, user.get());
            redirectAttributes.addFlashAttribute("success", "Assignment removed.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/assignments";
    }
}