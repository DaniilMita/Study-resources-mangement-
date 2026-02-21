package org.example.model;

public record StudyPlan(
        long id,
        long userId,
        String planType,
        String content,
        String createdAt
) {}