package com.skillbridge.service;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Service;

import com.skillbridge.entity.LearningResource;
import com.skillbridge.entity.User;
import com.skillbridge.repository.LearningResourceRepository;

@Service
public class LearningResourceService {

    private final LearningResourceRepository resourceRepository;

    public LearningResourceService(LearningResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public List<LearningResource> getAllResources() {
        return resourceRepository.findAllByOrderByCreatedAtDesc();
    }

    public LearningResource addResource(
            User owner,
            String title,
            String url,
            String topic,
            String description) {
        if (title == null || title.isBlank() || title.trim().length() > 160) {
            throw new RuntimeException("Enter a title of 160 characters or fewer.");
        }
        if (description != null && description.length() > 1000) {
            throw new RuntimeException("Description must be 1000 characters or fewer.");
        }
        URI resourceUri;
        try {
            resourceUri = URI.create(url == null ? "" : url.trim());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Enter a valid http or https link.");
        }
        String scheme = resourceUri.getScheme();
        if (scheme == null || resourceUri.getHost() == null
                || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new RuntimeException("Enter a valid http or https link.");
        }

        LearningResource resource = new LearningResource();
        resource.setOwner(owner);
        resource.setTitle(title.trim());
        resource.setUrl(resourceUri.toString());
        resource.setTopic(topic == null || topic.isBlank() ? null : topic.trim());
        resource.setDescription(description == null || description.isBlank()
                ? null
                : description.trim());
        return resourceRepository.save(resource);
    }

    public void deleteResource(Long resourceId, User owner) {
        LearningResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found."));
        if (!resource.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You cannot delete this resource.");
        }
        resourceRepository.delete(resource);
    }
}