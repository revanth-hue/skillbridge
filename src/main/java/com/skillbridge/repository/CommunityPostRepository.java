package com.skillbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.entity.CommunityPost;
import com.skillbridge.entity.Skill;
import com.skillbridge.entity.User;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    List<CommunityPost> findBySkillOrderByCreatedAtDesc(Skill skill);

    long countBySkill(Skill skill);

    @Transactional
    void deleteByAuthor(User author);
}