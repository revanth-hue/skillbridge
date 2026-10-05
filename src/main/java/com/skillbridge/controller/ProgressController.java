package com.skillbridge.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.skillbridge.entity.LearningAssignment;
import com.skillbridge.entity.SkillExchangeRequest;
import com.skillbridge.entity.User;
import com.skillbridge.service.CurrentUserService;
import com.skillbridge.service.LearningAssignmentService;
import com.skillbridge.service.SkillExchangeRequestService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProgressController {

    private final CurrentUserService currentUserService;
    private final LearningAssignmentService assignmentService;
    private final SkillExchangeRequestService requestService;

    public ProgressController(
            CurrentUserService currentUserService,
            LearningAssignmentService assignmentService,
            SkillExchangeRequestService requestService) {
        this.currentUserService = currentUserService;
        this.assignmentService = assignmentService;
        this.requestService = requestService;
    }

    @GetMapping("/progress")
    public String showProgress(HttpSession session, Model model) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        List<LearningAssignment> assignments = assignmentService.getAssignments(user.get());
        List<SkillExchangeRequest> sessions = requestService.getUserRequests(user.get());
        long completedAssignments = assignments.stream()
                .filter(LearningAssignment::isCompleted).count();
        long completedSessions = sessions.stream()
                .filter(item -> "COMPLETED".equals(item.getStatus())).count();
        long activeSessions = sessions.stream()
                .filter(item -> "ACCEPTED".equals(item.getStatus())
                        || "SCHEDULED".equals(item.getStatus())).count();
        long completed = completedAssignments + completedSessions;
        long total = assignments.size() + sessions.size();
        int completionRate = total == 0 ? 0 : (int) (completed * 100 / total);

        model.addAttribute("currentUser", user.get());
        model.addAttribute("completedAssignments", completedAssignments);
        model.addAttribute("openAssignments", assignments.size() - completedAssignments);
        model.addAttribute("completedSessions", completedSessions);
        model.addAttribute("activeSessions", activeSessions);
        model.addAttribute("completionRate", completionRate);
        return "progress";
    }
}