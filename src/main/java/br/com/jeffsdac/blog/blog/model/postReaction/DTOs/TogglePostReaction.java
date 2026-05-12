package br.com.jeffsdac.blog.blog.model.postReaction.DTOs;

import java.util.UUID;

import br.com.jeffsdac.blog.blog.model.postReaction.enums.ReactionType;
import jakarta.validation.constraints.NotNull;

public record TogglePostReaction(
        @NotNull(message = "postId is required")
        UUID postId,

        @NotNull(message = "reaction is required")
        ReactionType reaction) {

}
