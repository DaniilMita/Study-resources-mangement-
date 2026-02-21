package org.example.model;

public record LearningProfile(
     long userId,
     String mainSubject,
     String level,
     String goal,
     Integer dailyMinutes,
     String updateAt
) {}
