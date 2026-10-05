package com.skillbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillbridge.entity.LearningAssignment;
import com.skillbridge.entity.User;

public interface LearningAssignmentRepository extends JpaRepository<LearningAssignment, Long> {

    List<LearningAssignment> findByOwnerOrderByDueDateAscCreatedAtDesc(User owner);
}