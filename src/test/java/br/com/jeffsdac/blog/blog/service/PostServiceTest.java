package br.com.jeffsdac.blog.blog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import br.com.jeffsdac.blog.blog.model.posts.PostModel;
import br.com.jeffsdac.blog.blog.model.posts.dto.CreatePostDTO;
import br.com.jeffsdac.blog.blog.model.posts.dto.PostPublicDTO;
import br.com.jeffsdac.blog.blog.model.posts.dto.UpdatePostDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Captor
    private ArgumentCaptor<PostModel> postCaptor;

    @Test
    void getById_throws404_whenPostDoesNotExist() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.getById(postId));
    }

    @Test
    void getById_returnsPost_whenPostExists() {
        UUID postId = UUID.randomUUID();
        UserBlog author = createAuthor();

        PostModel post = createPost(postId, author, "My title", "My content");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        PostPublicDTO response = postService.getById(postId);

        assertEquals(postId, response.id());
        assertEquals("My title", response.title());
        assertEquals("My content", response.content());
        assertEquals(author.getId(), response.authorId());
        assertEquals(author.getUsername(), response.authorUsername());
    }

    @Test
    void create_setsAuthorAndVotes_whenValid() {
        UUID postId = UUID.randomUUID();
        UserBlog author = createAuthor();

        when(postRepository.save(any(PostModel.class))).thenAnswer(invocation -> {
            PostModel post = invocation.getArgument(0);
            post.setId(postId);
            return post;
        });

        PostPublicDTO response = postService.create(new CreatePostDTO("My title", "My content"), author);

        verify(postRepository).save(postCaptor.capture());
        assertEquals("My title", postCaptor.getValue().getTitle());
        assertEquals("My content", postCaptor.getValue().getContent());
        assertEquals(author, postCaptor.getValue().getAuthor());
        assertEquals(0, postCaptor.getValue().getLikeVotes());
        assertEquals(0, postCaptor.getValue().getUnlikeVotes());
        assertEquals(postId, response.id());
        assertEquals(author.getId(), response.authorId());
    }

    @Test
    void update_throws404_whenPostDoesNotExist() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> postService.update(postId, new UpdatePostDTO("New title", "New content"), createAuthor()));

        verify(postRepository, never()).save(any());
    }

    @Test
    void update_throws403_whenActorIsNotAuthor() {
        UUID postId = UUID.randomUUID();
        PostModel post = createPost(postId, createAuthor(), "Old title", "Old content");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        UserBlog actor = new UserBlog();
        actor.setId(UUID.randomUUID());
        actor.setUsername("actor");

        assertThrows(
                ForbiddenException.class,
                () -> postService.update(postId, new UpdatePostDTO("New title", "New content"), actor));

        verify(postRepository, never()).save(any());
    }

    @Test
    void update_returnsPost_whenActorIsAuthor() {
        UUID postId = UUID.randomUUID();
        UserBlog author = createAuthor();

        PostModel post = createPost(postId, author, "Old title", "Old content");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(PostModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostPublicDTO response = postService.update(
                postId,
                new UpdatePostDTO("New title", "New content"),
                author);

        verify(postRepository).save(postCaptor.capture());
        assertEquals("New title", postCaptor.getValue().getTitle());
        assertEquals("New content", postCaptor.getValue().getContent());
        assertEquals(postId, response.id());
        assertEquals("New title", response.title());
        assertEquals("New content", response.content());
        assertEquals(author.getId(), response.authorId());
    }

    private UserBlog createAuthor() {
        UserBlog author = new UserBlog();
        author.setId(UUID.randomUUID());
        author.setUsername("author");
        return author;
    }

    private PostModel createPost(UUID id, UserBlog author, String title, String content) {
        PostModel post = new PostModel();
        post.setId(id);
        post.setAuthor(author);
        post.setTitle(title);
        post.setContent(content);
        return post;
    }
}
