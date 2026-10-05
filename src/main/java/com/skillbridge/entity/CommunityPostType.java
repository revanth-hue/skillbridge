package com.skillbridge.entity;

public enum CommunityPostType {
    NEWS("News"),
    JOB("Job role"),
    INTERNSHIP("Internship"),
    FREELANCE("Freelance project");

    private final String label;

    CommunityPostType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}