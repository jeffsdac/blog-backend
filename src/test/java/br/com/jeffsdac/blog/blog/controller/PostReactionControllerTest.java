package br.com.jeffsdac.blog.blog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import br.com.jeffsdac.blog.blog.exception.ForbiddenException;
import br.com.jeffsdac.blog.blog.exception.NotFoundException;
import br.com.jeffsdac.blog.blog.model.postReaction.DTOs.PostReactionResponseDTO;
import br.com.jeffsdac.blog.blog.model.postReaction.DTOs.TogglePostReaction;
import br.com.jeffsdac.blog.blog.model.postReaction.enums.PostReactionAction;
import br.com.jeffsdac.blog.blog.model.postReaction.enums.ReactionType;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.repository.UserBlogRepository;
import br.com.jeffsdac.blog.blog.service.PostReactionService;
import br.com.jeffsdac.blog.blog.service.TokenService;

@WebMvcTest(controllers = PostReactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class PostReactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostReactionController postReactionController;

    @MockitoBean
    private PostReactionService postReactionService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserBlogRepository userBlogRepository;

    @Test
    void saveReaction_returns200_whenReactionCreated() throws Exception {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(postReactionService.react(any(TogglePostReaction.class), any(UserBlog.class)))
                .thenReturn(new PostReactionResponseDTO(
                        postId,
                        userId,
                        ReactionType.LIKE,
                        PostReactionAction.CREATED,
                        1L,
                        0L));

        mockMvc.perform(
                post("/api/v1/post-reactor")
                        .with(authUser(userId, "jeffsdac"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postId":"%s","reaction":"LIKE"}
                                """.formatted(postId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.currentReaction").value("LIKE"))
                .andExpect(jsonPath("$.action").value("CREATED"))
                .andExpect(jsonPath("$.likeVotes").value(1))
                .andExpect(jsonPath("$.deslikeVotes").value(0));
    }

    @Test
    void saveReaction_returns200_whenReactionRemoved() throws Exception {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(postReactionService.react(any(TogglePostReaction.class), any(UserBlog.class)))
                .thenReturn(new PostReactionResponseDTO(
                        postId,
                        userId,
                        null,
                        PostReactionAction.REMOVED,
                        0L,
                        0L));

        mockMvc.perform(
                post("/api/v1/post-reactor")
                        .with(authUser(userId, "jeffsdac"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postId":"%s","reaction":"LIKE"}
                                """.formatted(postId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentReaction").doesNotExist())
                .andExpect(jsonPath("$.action").value("REMOVED"))
                .andExpect(jsonPath("$.likeVotes").value(0))
                .andExpect(jsonPath("$.deslikeVotes").value(0));
    }

    @Test
    void saveReaction_passesAuthenticatedUserToService() throws Exception {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserBlog authenticatedUser = new UserBlog();
        authenticatedUser.setId(userId);
        authenticatedUser.setUsername("jeffsdac");

        when(postReactionService.react(any(TogglePostReaction.class), any(UserBlog.class)))
                .thenReturn(new PostReactionResponseDTO(
                        postId,
                        userId,
                        ReactionType.DESLIKE,
                        PostReactionAction.CREATED,
                        0L,
                        1L));

        postReactionController.saveReaction(new TogglePostReaction(postId, ReactionType.DESLIKE), authenticatedUser);

        ArgumentCaptor<UserBlog> userCaptor = ArgumentCaptor.forClass(UserBlog.class);
        verify(postReactionService).react(
                eq(new TogglePostReaction(postId, ReactionType.DESLIKE)),
                userCaptor.capture());
        assertEquals(userId, userCaptor.getValue().getId());
        assertEquals("jeffsdac", userCaptor.getValue().getUsername());
    }

    @Test
    void saveReaction_returns400_whenPostIdIsMissing() throws Exception {
        mockMvc.perform(
                post("/api/v1/post-reactor")
                        .with(authUser(UUID.randomUUID(), "jeffsdac"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reaction":"LIKE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.postId").exists());
    }

    @Test
    void saveReaction_returns400_whenReactionIsMissing() throws Exception {
        mockMvc.perform(
                post("/api/v1/post-reactor")
                        .with(authUser(UUID.randomUUID(), "jeffsdac"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postId":"%s"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.reaction").exists());
    }

    @Test
    void saveReaction_returns404_whenServiceThrowsNotFound() throws Exception {
        UUID postId = UUID.randomUUID();
        when(postReactionService.react(any(TogglePostReaction.class), any(UserBlog.class)))
                .thenThrow(new NotFoundException("Post not found."));

        mockMvc.perform(
                post("/api/v1/post-reactor")
                        .with(authUser(UUID.randomUUID(), "jeffsdac"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postId":"%s","reaction":"LIKE"}
                                """.formatted(postId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Post not found."));
    }

    @Test
    void saveReaction_returns403_whenServiceThrowsForbidden() throws Exception {
        UUID postId = UUID.randomUUID();
        when(postReactionService.react(any(TogglePostReaction.class), any(UserBlog.class)))
                .thenThrow(new ForbiddenException("Authenticated user is required to react to a post."));

        mockMvc.perform(
                post("/api/v1/post-reactor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postId":"%s","reaction":"LIKE"}
                                """.formatted(postId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Authenticated user is required to react to a post."));
    }

    private static RequestPostProcessor authUser(UUID id, String username) {
        return request -> {
            UserBlog principal = new UserBlog();
            principal.setId(id);
            principal.setUsername(username);
            var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            return authentication(auth).postProcessRequest(request);
        };
    }
}
