package com.skillbridge.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.skillbridge.entity.CommunityPostType;
import com.skillbridge.entity.Skill;
import com.skillbridge.entity.User;
import com.skillbridge.service.CommunityService;
import com.skillbridge.service.CurrentUserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CommunityController {

    private final CommunityService communityService;
    private final CurrentUserService currentUserService;

    public CommunityController(
            CommunityService communityService,
            CurrentUserService currentUserService) {
        this.communityService = communityService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/community")
    public String showCommunity(
            @RequestParam(required = false) Long skillId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        Optional<User> currentUser = currentUserService.findCurrentUser(session);
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        List<Skill> skills = communityService.getSkills();
        Map<Long, Long> postCounts = new LinkedHashMap<>();
        for (Skill skill : skills) {
            postCounts.put(skill.getId(), communityService.countPosts(skill));
        }

        Skill selectedSkill = null;
        if (skillId != null) {
            try {
                selectedSkill = communityService.getSkill(skillId);
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/community";
            }
        }

        model.addAttribute("currentUser", currentUser.get());
        model.addAttribute("skills", skills);
        model.addAttribute("postCounts", postCounts);
        model.addAttribute("selectedSkill", selectedSkill);
        model.addAttribute("posts", selectedSkill == null
                ? List.of()
                : communityService.getPosts(selectedSkill));
        model.addAttribute("postTypes", CommunityPostType.values());
        return "community";
    }

    @PostMapping("/community/posts")
    public String createPost(
            @RequestParam Long skillId,
            @RequestParam String postType,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String linkUrl,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<User> currentUser = currentUserService.findCurrentUser(session);
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        try {
            communityService.createPost(currentUser.get(), skillId, postType,
                    title, content, linkUrl);
            redirectAttributes.addFlashAttribute("success", "Your post is live in this skill community.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/community?skillId=" + skillId;
    }
}