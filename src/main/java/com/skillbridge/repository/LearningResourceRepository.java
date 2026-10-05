package com.skillbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillbridge.entity.LearningResource;

public interface LearningResourceRepository extends JpaRepository<LearningResource, Long> {

    List<LearningResource> findAllByOrderByCreatedAtDesc();
}