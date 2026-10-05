package com.skillbridge.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.skillbridge.entity.SkillExchangeRequest;
import com.skillbridge.entity.User;
import com.skillbridge.service.CurrentUserService;
import com.skillbridge.service.SkillExchangeRequestService;

import jakarta.servlet.http.HttpSession;

@Controller
public class SkillExchangeRequestController {

    private final SkillExchangeRequestService requestService;
    private final CurrentUserService currentUserService;

    public SkillExchangeRequestController(
            SkillExchangeRequestService requestService,
            CurrentUserService currentUserService) {

        this.requestService = requestService;
        this.currentUserService = currentUserService;
    }

    // Show requests page
    @GetMapping("/requests")
    public String showRequestsPage(
            HttpSession session,
            Model model) {

        Optional<User> userOptional = currentUserService.findCurrentUser(session);

        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOptional.get();

        List<SkillExchangeRequest> receivedRequests =
                requestService.getReceivedRequests(user);

        List<SkillExchangeRequest> sentRequests =
                requestService.getSentRequests(user);

        model.addAttribute("user", user);
        model.addAttribute("receivedRequests", receivedRequests);
        model.addAttribute("sentRequests", sentRequests);

        return "requests";
    }

    // Send request
    @PostMapping("/requests/send")
    public String sendRequest(
            @RequestParam Long receiverId,
            @RequestParam(required = false) String message,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Optional<User> sender = currentUserService.findCurrentUser(session);
        if (sender.isEmpty()) {
            return "redirect:/login";
        }

        try {
            requestService.sendRequest(
                    sender.get(),
                    receiverId,
                    message
            );
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/skill-matching";
        }

        redirectAttributes.addFlashAttribute("success", "Request sent.");
        return "redirect:/requests";
    }

    // Accept request
    @PostMapping("/requests/accept")
    public String acceptRequest(
            @RequestParam Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        return updateRequestStatus(requestId, true, session, redirectAttributes);
    }

    // Reject request
    @PostMapping("/requests/reject")
    public String rejectRequest(
            @RequestParam Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        return updateRequestStatus(requestId, false, session, redirectAttributes);
    }

    private String updateRequestStatus(
            Long requestId,
            boolean accept,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        try {
            if (accept) {
                requestService.acceptRequest(requestId, user.get());
            } else {
                requestService.rejectRequest(requestId, user.get());
            }
            redirectAttributes.addFlashAttribute("success", "Request updated.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/requests";
    }
}