package br.com.jeffsdac.blog.blog.model.userBlog.dto;

import java.util.UUID;

public record UserBlogPublicDTO(
        UUID id,
        String username,
        String email) {
}

