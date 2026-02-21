package org.example.model;

public record User (
        long id,
        String email,
        String passwordHash,
        String createdAt
) {}
