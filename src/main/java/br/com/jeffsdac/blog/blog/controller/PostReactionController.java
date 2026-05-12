package br.com.jeffsdac.blog.blog.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.jeffsdac.blog.blog.model.postReaction.DTOs.PostReactionResponseDTO;
import br.com.jeffsdac.blog.blog.model.postReaction.DTOs.TogglePostReaction;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.service.PostReactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/post-reactor")
public class PostReactionController {

    private static final Logger log = LoggerFactory.getLogger(PostReactionController.class);

    private final PostReactionService postReactionService;

    public PostReactionController(PostReactionService postReactionService) {
        this.postReactionService = postReactionService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<PostReactionResponseDTO> saveReaction(@RequestBody @Valid TogglePostReaction dto,
            @AuthenticationPrincipal UserBlog user) {
        log.debug("POST /api/v1/post-reactor postId={} reaction={} authenticatedUser={}",
                dto.postId(),
                dto.reaction(),
                user == null ? "null" : user.getUsername());

        PostReactionResponseDTO reaction = postReactionService.react(dto, user);

        log.debug("POST /api/v1/post-reactor completed postId={} userId={} action={} currentReaction={}",
                reaction.postId(),
                reaction.userId(),
                reaction.action(),
                reaction.currentReaction());

        return ResponseEntity.status(200).body(reaction);
    }

}
