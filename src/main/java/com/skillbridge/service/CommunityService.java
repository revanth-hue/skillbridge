package com.skillbridge.service;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.skillbridge.entity.CommunityPost;
import com.skillbridge.entity.CommunityPostType;
import com.skillbridge.entity.Skill;
import com.skillbridge.entity.User;
import com.skillbridge.repository.CommunityPostRepository;
import com.skillbridge.repository.SkillRepository;

@Service
public class CommunityService {

    private final CommunityPostRepository postRepository;
    private final SkillRepository skillRepository;

    public CommunityService(
            CommunityPostRepository postRepository,
            SkillRepository skillRepository) {
        this.postRepository = postRepository;
        this.skillRepository = skillRepository;
    }

    public List<Skill> getSkills() {
        return skillRepository.findAll();
    }

    public Skill getSkill(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill community not found."));
    }

    public List<CommunityPost> getPosts(Skill skill) {
        return postRepository.findBySkillOrderByCreatedAtDesc(skill);
    }

    public long countPosts(Skill skill) {
        return postRepository.countBySkill(skill);
    }

    public CommunityPost createPost(
            User author,
            Long skillId,
            String type,
            String title,
            String content,
            String linkUrl) {
        if (title == null || title.isBlank() || title.trim().length() > 180) {
            throw new RuntimeException("Title is required and must be 180 characters or fewer.");
        }
        if (content == null || content.isBlank() || content.trim().length() > 5000) {
            throw new RuntimeException("Post details are required and must be 5000 characters or fewer.");
        }

        CommunityPostType postType;
        try {
            postType = CommunityPostType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RuntimeException("Choose news, a job, an internship, or freelance work.");
        }

        String validatedLink = validateLink(linkUrl);
        Skill skill = getSkill(skillId);
        CommunityPost post = new CommunityPost();
        post.setSkill(skill);
        post.setAuthor(author);
        post.setPostType(postType);
        post.setTitle(title.trim());
        post.setContent(content.trim());
        post.setLinkUrl(validatedLink);
        return postRepository.save(post);
    }

    private String validateLink(String linkUrl) {
        if (linkUrl == null || linkUrl.isBlank()) {
            return null;
        }
        if (linkUrl.trim().length() > 1000) {
            throw new RuntimeException("Link must be 1000 characters or fewer.");
        }

        URI uri;
        try {
            uri = URI.create(linkUrl.trim());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Enter a valid http or https link.");
        }
        String scheme = uri.getScheme();
        if (uri.getHost() == null || scheme == null
                || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
            throw new RuntimeException("Enter a valid http or https link.");
        }
        return uri.toString();
    }
}