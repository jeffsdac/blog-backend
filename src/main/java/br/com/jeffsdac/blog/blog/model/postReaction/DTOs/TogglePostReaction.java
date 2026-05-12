package br.com.jeffsdac.blog.blog.model.postReaction.DTOs;

import java.util.UUID;

import br.com.jeffsdac.blog.blog.model.postReaction.enums.ReactionType;

public record TogglePostReaction(UUID postId, UUID userId, ReactionType reaction) {

}
