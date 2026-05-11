package br.com.jeffsdac.blog.blog.model.posts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostDTO(
        @NotBlank(message = "title is required")
        @Size(min = 3, max = 150, message = "title must be between 3 and 150 characters")
        String title,

        @NotBlank(message = "content is required")
        @Size(min = 3, max = 10000, message = "content must be between 3 and 10000 characters")
        String content) {
}

