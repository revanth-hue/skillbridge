package com.skillbridge.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.entity.Skill;
import com.skillbridge.entity.User;
import com.skillbridge.repository.UserRepository;

@Service
public class SkillMatchingService {

    private final UserRepository userRepository;

    public SkillMatchingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

        @Transactional(readOnly = true)
        public List<User> findMatches(User currentUser) {

        User matchingUser = userRepository.findById(currentUser.getId())
            .orElse(currentUser);

        List<User> allUsers = userRepository.findAll();
        List<User> matches = new ArrayList<>();

        Set<Skill> currentUserWantsToLearn =
            matchingUser.getSkillsToLearn();

        Set<Skill> currentUserCanTeach =
            matchingUser.getSkillsToTeach();

        for (User otherUser : allUsers) {

            // Don't match the user with themselves
            if (otherUser.getId().equals(matchingUser.getId())) {
                continue;
            }

            Set<Skill> otherUserCanTeach =
                    otherUser.getSkillsToTeach();

            Set<Skill> otherUserWantsToLearn =
                    otherUser.getSkillsToLearn();

            // Check:
            // Current user wants to learn something
            // that the other user can teach
            boolean canTeachWhatINeed =
                    hasCommonSkill(
                            currentUserWantsToLearn,
                            otherUserCanTeach
                    );

            // Check:
            // Current user can teach something
            // that the other user wants to learn
            boolean canLearnWhatTheyTeach =
                    hasCommonSkill(
                            currentUserCanTeach,
                            otherUserWantsToLearn
                    );

            // Mutual skill exchange
            if (canTeachWhatINeed && canLearnWhatTheyTeach) {
                matches.add(otherUser);
            }
        }

        return matches;
    }

    private boolean hasCommonSkill(
            Set<Skill> firstSet,
            Set<Skill> secondSet) {

        for (Skill firstSkill : firstSet) {

            for (Skill secondSkill : secondSet) {

                if (firstSkill.getId().equals(secondSkill.getId())) {
                    return true;
                }
            }
        }

        return false;
    }
}