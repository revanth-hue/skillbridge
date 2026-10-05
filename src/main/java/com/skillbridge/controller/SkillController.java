package com.skillbridge.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.skillbridge.entity.Skill;
import com.skillbridge.entity.User;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.CurrentUserService;
import com.skillbridge.service.SkillService;

import jakarta.servlet.http.HttpSession;

@Controller
public class SkillController {

    private final SkillService skillService;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public SkillController(
            SkillService skillService,
            CurrentUserService currentUserService,
            UserRepository userRepository) {

        this.skillService = skillService;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my-skills")
    public String showMySkillsPage(
            HttpSession session,
            Model model) {

        Optional<User> user = currentUserService.findCurrentUser(session);

        if (user.isEmpty()) {
            return "redirect:/login";
        }

        model.addAttribute("user", user.get());
        model.addAttribute("skills", skillService.getAllSkills());

        return "my-skills";
    }

    @PostMapping("/my-skills/save")
    public String saveMySkills(
            @RequestParam(required = false) Long[] teachingSkills,
            @RequestParam(required = false) Long[] learningSkills,
            HttpSession session) {

        Optional<User> userOptional = currentUserService.findCurrentUser(session);

        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOptional.get();

        user.getSkillsToTeach().clear();
        user.getSkillsToLearn().clear();

        if (teachingSkills != null) {
            for (Long skillId : teachingSkills) {
                Optional<Skill> skill = skillService.getSkillById(skillId);

                skill.ifPresent(s ->
                        user.getSkillsToTeach().add(s));
            }
        }

        if (learningSkills != null) {
            for (Long skillId : learningSkills) {
                Optional<Skill> skill = skillService.getSkillById(skillId);

                skill.ifPresent(s ->
                        user.getSkillsToLearn().add(s));
            }
        }

        userRepository.save(user);

        return "redirect:/my-skills";
    }
}