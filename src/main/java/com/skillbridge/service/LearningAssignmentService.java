package com.skillbridge.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.skillbridge.entity.LearningAssignment;
import com.skillbridge.entity.User;
import com.skillbridge.repository.LearningAssignmentRepository;

@Service
public class LearningAssignmentService {

    private final LearningAssignmentRepository assignmentRepository;

    public LearningAssignmentService(LearningAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    public List<LearningAssignment> getAssignments(User owner) {
        return assignmentRepository.findByOwnerOrderByDueDateAscCreatedAtDesc(owner);
    }

    public LearningAssignment addAssignment(
            User owner,
            String title,
            String details,
            LocalDate dueDate) {
        if (title == null || title.isBlank() || title.trim().length() > 160) {
            throw new RuntimeException("Enter a title of 160 characters or fewer.");
        }
        if (details != null && details.length() > 1000) {
            throw new RuntimeException("Details must be 1000 characters or fewer.");
        }
        LearningAssignment assignment = new LearningAssignment();
        assignment.setOwner(owner);
        assignment.setTitle(title.trim());
        assignment.setDetails(details == null || details.isBlank() ? null : details.trim());
        assignment.setDueDate(dueDate);
        return assignmentRepository.save(assignment);
    }

    public LearningAssignment setCompleted(Long assignmentId, User owner, boolean completed) {
        LearningAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found."));
        if (!assignment.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You cannot update this assignment.");
        }
        assignment.setCompleted(completed);
        return assignmentRepository.save(assignment);
    }

    public void deleteAssignment(Long assignmentId, User owner) {
        LearningAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found."));
        if (!assignment.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You cannot delete this assignment.");
        }
        assignmentRepository.delete(assignment);
    }
}