package br.com.jeffsdac.blog.blog.model.postReaction.DTOs;

import java.util.UUID;

import br.com.jeffsdac.blog.blog.model.postReaction.enums.PostReactionAction;
import br.com.jeffsdac.blog.blog.model.postReaction.enums.ReactionType;

public record PostReactionResponseDTO(
        UUID postId,
        UUID userId,
        ReactionType currentReaction,
        PostReactionAction action,
        long likeVotes,
        long deslikeVotes) {

}
