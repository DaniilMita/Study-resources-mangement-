package org.example.model;

public record ChatMessage(
        long id,
        long userId,
        String role,
        String message,
        String timestamp
) {}