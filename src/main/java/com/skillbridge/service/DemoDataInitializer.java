package com.skillbridge.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.skillbridge.entity.Skill;
import com.skillbridge.entity.User;
import com.skillbridge.repository.SkillRepository;
import com.skillbridge.repository.UserRepository;

@Component
@Profile("!prod")
public class DemoDataInitializer implements CommandLineRunner {

    public static final String DEMO_PASSWORD = "SkillBridgeDemo2026!";

    private static final String[] FIRST_NAMES = {
        "Aarav", "Maya", "Rohan", "Anika", "Kabir",
        "Isha", "Neil", "Tara", "Dev", "Meera"
    };

    private static final String[] LAST_NAMES = {
        "Shah", "Kapoor", "Reddy", "Mehta", "Patel"
    };

    private static final List<Skill> STARTER_SKILLS = List.of(
            new Skill("Java Development", "Programming", "Build applications with Java and object-oriented design."),
            new Skill("Web Development", "Technology", "Create accessible, responsive websites and web apps."),
            new Skill("Data Analysis", "Data", "Explore datasets and communicate useful findings."),
            new Skill("UI/UX Design", "Design", "Research, wireframe, and prototype digital products."),
            new Skill("Digital Photography", "Creative", "Learn composition, lighting, and editing."),
            new Skill("English Conversation", "Language", "Practice everyday spoken English and confidence."),
            new Skill("Public Speaking", "Communication", "Prepare and deliver clear, engaging presentations."),
            new Skill("Project Management", "Business", "Plan work, manage risks, and deliver projects."));

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public DemoDataInitializer(
            UserRepository userRepository,
            SkillRepository skillRepository) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    public void run(String... args) {
        List<Skill> skills = ensureStarterSkills();
        long userCount = userRepository.count();
        if (userCount >= 50) {
            return;
        }

        Set<String> existingEmails = new HashSet<>();
        userRepository.findAll().forEach(user -> existingEmails.add(user.getEmail()));
        String encodedPassword = new BCryptPasswordEncoder().encode(DEMO_PASSWORD);
        List<User> newUsers = new ArrayList<>();

        for (int memberNumber = 1; memberNumber <= 50 && userCount < 50; memberNumber++) {
            String email = String.format(Locale.ROOT,
                    "demo.member%02d@skillbridge.demo", memberNumber);
            if (!existingEmails.add(email)) {
                continue;
            }

            int personIndex = memberNumber - 1;
            int pairSkillIndex = (personIndex / 2) % skills.size();
            Skill firstSkill = skills.get(pairSkillIndex);
            Skill secondSkill = skills.get((pairSkillIndex + 1) % skills.size());
            boolean isFirstInPair = personIndex % 2 == 0;

            User user = new User();
            user.setFullName(FIRST_NAMES[personIndex % FIRST_NAMES.length]
                    + " " + LAST_NAMES[personIndex / FIRST_NAMES.length]);
            user.setEmail(email);
            user.setPassword(encodedPassword);
            user.setBio("Demo member who enjoys sharing "
                    + (isFirstInPair ? firstSkill.getName() : secondSkill.getName())
                    + " and learning "
                    + (isFirstInPair ? secondSkill.getName() : firstSkill.getName())
                    + ".");
            user.getSkillsToTeach().add(isFirstInPair ? firstSkill : secondSkill);
            user.getSkillsToLearn().add(isFirstInPair ? secondSkill : firstSkill);
            newUsers.add(user);
            userCount++;
        }

        userRepository.saveAll(newUsers);
    }

    private List<Skill> ensureStarterSkills() {
        List<Skill> skills = new ArrayList<>();
        for (Skill starterSkill : STARTER_SKILLS) {
            skills.add(skillRepository.findByName(starterSkill.getName())
                    .orElseGet(() -> skillRepository.save(starterSkill)));
        }
        return skills;
    }
}