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
import com.skillbridge.service.LearningResourceService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LearningResourceController {

    private final CurrentUserService currentUserService;
    private final LearningResourceService resourceService;

    public LearningResourceController(
            CurrentUserService currentUserService,
            LearningResourceService resourceService) {
        this.currentUserService = currentUserService;
        this.resourceService = resourceService;
    }

    @GetMapping("/resources")
    public String showResources(HttpSession session, Model model) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        model.addAttribute("currentUser", user.get());
        model.addAttribute("resources", resourceService.getAllResources());
        return "resources";
    }

    @PostMapping("/resources/add")
    public String addResource(
            @RequestParam String title,
            @RequestParam String url,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String description,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        try {
            resourceService.addResource(user.get(), title, url, topic, description);
            redirectAttributes.addFlashAttribute("success", "Resource shared.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/resources";
    }

    @PostMapping("/resources/delete")
    public String deleteResource(
            @RequestParam Long resourceId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<User> user = currentUserService.findCurrentUser(session);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        try {
            resourceService.deleteResource(resourceId, user.get());
            redirectAttributes.addFlashAttribute("success", "Resource removed.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/resources";
    }
}