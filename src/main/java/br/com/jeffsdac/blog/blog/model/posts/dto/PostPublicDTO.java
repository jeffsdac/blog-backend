package br.com.jeffsdac.blog.blog.model.posts.dto;

import java.time.Instant;
import java.util.UUID;

public record PostPublicDTO(
        UUID id,
        String title,
        String content,
        UUID authorId,
        String authorUsername,
        int likeVotes,
        int unlikeVotes,
        Instant createdAt) {
}

