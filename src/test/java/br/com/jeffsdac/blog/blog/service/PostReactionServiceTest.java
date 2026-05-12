package br.com.jeffsdac.blog.blog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.jeffsdac.blog.blog.exception.ForbiddenException;
import br.com.jeffsdac.blog.blog.exception.NotFoundException;
import br.com.jeffsdac.blog.blog.model.postReaction.DTOs.PostReactionResponseDTO;
import br.com.jeffsdac.blog.blog.model.postReaction.DTOs.TogglePostReaction;
import br.com.jeffsdac.blog.blog.model.postReaction.PostReaction;
import br.com.jeffsdac.blog.blog.model.postReaction.enums.PostReactionAction;
import br.com.jeffsdac.blog.blog.model.postReaction.enums.ReactionType;
import br.com.jeffsdac.blog.blog.model.posts.PostModel;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.repository.PostReactionRepository;
import br.com.jeffsdac.blog.blog.repository.PostRepository;
import br.com.jeffsdac.blog.blog.repository.UserBlogRepository;

@ExtendWith(MockitoExtension.class)
class PostReactionServiceTest {

    @Mock
    private PostReactionRepository postReactionRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserBlogRepository userBlogRepository;

    @InjectMocks
    private PostReactionService postReactionService;

    @Captor
    private ArgumentCaptor<PostReaction> reactionCaptor;

    @Test
    void react_createsReaction_whenUserHasNoReaction() {
        UUID postId = UUID.randomUUID();
        UserBlog authenticatedUser = createUser(UUID.randomUUID(), "jeffsdac");
        PostModel post = createPost(postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userBlogRepository.findById(authenticatedUser.getId())).thenReturn(Optional.of(authenticatedUser));
        when(postReactionRepository.findByPostIdAndUserId(postId, authenticatedUser.getId()))
                .thenReturn(Optional.empty());
        when(postReactionRepository.save(any(PostReaction.class))).thenAnswer(invocation -> {
            PostReaction reaction = invocation.getArgument(0);
            reaction.setId(UUID.randomUUID());
            return reaction;
        });
        when(postReactionRepository.countByPostIdAndType(postId, ReactionType.LIKE)).thenReturn(1L);
        when(postReactionRepository.countByPostIdAndType(postId, ReactionType.DESLIKE)).thenReturn(0L);

        PostReactionResponseDTO response = postReactionService.react(
                new TogglePostReaction(postId, ReactionType.LIKE),
                authenticatedUser);

        verify(postReactionRepository).save(reactionCaptor.capture());
        assertEquals(post, reactionCaptor.getValue().getPostModel());
        assertEquals(authenticatedUser, reactionCaptor.getValue().getUserBlog());
        assertEquals(ReactionType.LIKE, reactionCaptor.getValue().getType());
        assertEquals(PostReactionAction.CREATED, response.action());
        assertEquals(ReactionType.LIKE, response.currentReaction());
        assertEquals(1L, response.likeVotes());
        assertEquals(0L, response.deslikeVotes());
    }

    @Test
    void react_removesReaction_whenSameReactionAlreadyExists() {
        UUID postId = UUID.randomUUID();
        UserBlog authenticatedUser = createUser(UUID.randomUUID(), "jeffsdac");
        PostModel post = createPost(postId);
        PostReaction existingReaction = createReaction(post, authenticatedUser, ReactionType.LIKE);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userBlogRepository.findById(authenticatedUser.getId())).thenReturn(Optional.of(authenticatedUser));
        when(postReactionRepository.findByPostIdAndUserId(postId, authenticatedUser.getId()))
                .thenReturn(Optional.of(existingReaction));
        when(postReactionRepository.countByPostIdAndType(postId, ReactionType.LIKE)).thenReturn(0L);
        when(postReactionRepository.countByPostIdAndType(postId, ReactionType.DESLIKE)).thenReturn(0L);

        PostReactionResponseDTO response = postReactionService.react(
                new TogglePostReaction(postId, ReactionType.LIKE),
                authenticatedUser);

        verify(postReactionRepository).delete(existingReaction);
        verify(postReactionRepository, never()).save(any());
        assertEquals(PostReactionAction.REMOVED, response.action());
        assertNull(response.currentReaction());
        assertEquals(0L, response.likeVotes());
        assertEquals(0L, response.deslikeVotes());
    }

    @Test
    void react_updatesReaction_whenLikeChangesToDeslike() {
        UUID postId = UUID.randomUUID();
        UserBlog authenticatedUser = createUser(UUID.randomUUID(), "jeffsdac");
        PostModel post = createPost(postId);
        PostReaction existingReaction = createReaction(post, authenticatedUser, ReactionType.LIKE);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userBlogRepository.findById(authenticatedUser.getId())).thenReturn(Optional.of(authenticatedUser));
        when(postReactionRepository.findByPostIdAndUserId(postId, authenticatedUser.getId()))
                .thenReturn(Optional.of(existingReaction));
        when(postReactionRepository.countByPostIdAndType(postId, ReactionType.LIKE)).thenReturn(0L);
        when(postReactionRepository.countByPostIdAndType(postId, ReactionType.DESLIKE)).thenReturn(1L);

        PostReactionResponseDTO response = postReactionService.react(
                new TogglePostReaction(postId, ReactionType.DESLIKE),
                authenticatedUser);

        verify(postReactionRepository).save(existingReaction);
        assertEquals(ReactionType.DESLIKE, existingReaction.getType());
        assertEquals(PostReactionAction.UPDATED, response.action());
        assertEquals(ReactionType.DESLIKE, response.currentReaction());
        assertEquals(0L, response.likeVotes());
        assertEquals(1L, response.deslikeVotes());
    }

    @Test
    void react_updatesReaction_whenDeslikeChangesToLike() {
        UUID postId = UUID.randomUUID();
        UserBlog authenticatedUser = createUser(UUID.randomUUID(), "jeffsdac");
        PostModel post = createPost(postId);
        PostReaction existingReaction = createReaction(post, authenticatedUser, ReactionType.DESLIKE);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userBlogRepository.findById(authenticatedUser.getId())).thenReturn(Optional.of(authenticatedUser));
        when(postReactionRepository.findByPostIdAndUserId(postId, authenticatedUser.getId()))
                .thenReturn(Optional.of(existingReaction));
        when(postReactionRepository.countByPostIdAndType(postId, ReactionType.LIKE)).thenReturn(1L);
        when(postReactionRepository.countByPostIdAndType(postId, ReactionType.DESLIKE)).thenReturn(0L);

        PostReactionResponseDTO response = postReactionService.react(
                new TogglePostReaction(postId, ReactionType.LIKE),
                authenticatedUser);

        verify(postReactionRepository).save(existingReaction);
        assertEquals(ReactionType.LIKE, existingReaction.getType());
        assertEquals(PostReactionAction.UPDATED, response.action());
        assertEquals(ReactionType.LIKE, response.currentReaction());
        assertEquals(1L, response.likeVotes());
        assertEquals(0L, response.deslikeVotes());
    }

    @Test
    void react_throws404_whenPostDoesNotExist() {
        UUID postId = UUID.randomUUID();
        UserBlog authenticatedUser = createUser(UUID.randomUUID(), "jeffsdac");
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> postReactionService.react(new TogglePostReaction(postId, ReactionType.LIKE), authenticatedUser));

        verify(userBlogRepository, never()).findById(any());
        verify(postReactionRepository, never()).findByPostIdAndUserId(any(), any());
        verify(postReactionRepository, never()).save(any());
        verify(postReactionRepository, never()).delete(any());
    }

    @Test
    void react_throws404_whenAuthenticatedUserDoesNotExist() {
        UUID postId = UUID.randomUUID();
        UserBlog authenticatedUser = createUser(UUID.randomUUID(), "jeffsdac");
        PostModel post = createPost(postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userBlogRepository.findById(authenticatedUser.getId())).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> postReactionService.react(new TogglePostReaction(postId, ReactionType.LIKE), authenticatedUser));

        verify(postReactionRepository, never()).findByPostIdAndUserId(any(), any());
        verify(postReactionRepository, never()).save(any());
        verify(postReactionRepository, never()).delete(any());
    }

    @Test
    void react_throws403_whenAuthenticatedUserIsNull() {
        assertThrows(
                ForbiddenException.class,
                () -> postReactionService.react(
                        new TogglePostReaction(UUID.randomUUID(), ReactionType.LIKE),
                        null));

        verify(postRepository, never()).findById(any());
        verify(userBlogRepository, never()).findById(any());
        verify(postReactionRepository, never()).save(any());
    }

    @Test
    void react_usesAuthenticatedUserId_whenSearchingExistingReaction() {
        UUID postId = UUID.randomUUID();
        UserBlog authenticatedUser = createUser(UUID.randomUUID(), "real-user");
        PostModel post = createPost(postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userBlogRepository.findById(authenticatedUser.getId())).thenReturn(Optional.of(authenticatedUser));
        when(postReactionRepository.findByPostIdAndUserId(postId, authenticatedUser.getId()))
                .thenReturn(Optional.empty());
        when(postReactionRepository.save(any(PostReaction.class))).thenAnswer(invocation -> {
            PostReaction reaction = invocation.getArgument(0);
            reaction.setId(UUID.randomUUID());
            return reaction;
        });

        postReactionService.react(new TogglePostReaction(postId, ReactionType.LIKE), authenticatedUser);

        verify(userBlogRepository).findById(authenticatedUser.getId());
        verify(postReactionRepository).findByPostIdAndUserId(postId, authenticatedUser.getId());
    }

    private UserBlog createUser(UUID id, String username) {
        UserBlog user = new UserBlog();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private PostModel createPost(UUID id) {
        PostModel post = new PostModel();
        post.setId(id);
        post.setTitle("Post title");
        post.setContent("Post content");
        return post;
    }

    private PostReaction createReaction(PostModel post, UserBlog user, ReactionType type) {
        PostReaction reaction = new PostReaction(post, user, type);
        reaction.setId(UUID.randomUUID());
        return reaction;
    }
}
