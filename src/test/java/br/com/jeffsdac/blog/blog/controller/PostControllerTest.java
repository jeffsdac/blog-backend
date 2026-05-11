package br.com.jeffsdac.blog.blog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import br.com.jeffsdac.blog.blog.exception.NotFoundException;
import br.com.jeffsdac.blog.blog.model.posts.dto.PostPublicDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.repository.UserBlogRepository;
import br.com.jeffsdac.blog.blog.service.PostService;
import br.com.jeffsdac.blog.blog.service.TokenService;

@WebMvcTest(controllers = PostController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class PostControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private PostService postService;

        @MockitoBean
        private TokenService tokenService;

        @MockitoBean
        private UserBlogRepository userBlogRepository;

        private static RequestPostProcessor authUser(String username) {
                return request -> {
                        UserBlog principal = new UserBlog();
                        principal.setUsername(username);
                        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        return request;
                };
        }

        @Test
        void get_returns404_whenNotFound() throws Exception {
                UUID id = UUID.randomUUID();
                when(postService.getById(any(UUID.class))).thenThrow(new NotFoundException("Post not found."));
                mockMvc.perform(get("/api/v1/posts/{id}", id))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        void create_returns201_whenAuthenticated() throws Exception {
                UUID id = UUID.randomUUID();
                when(postService.create(any(), any())).thenReturn(
                                new PostPublicDTO(id, "t", "c", UUID.randomUUID(), "user1", 0, 0, Instant.now()));

                mockMvc.perform(
                                post("/api/v1/posts")
                                                .with(authUser("user1"))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {"title":"My title","content":"My content"}
                                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        void create_returns400_whenPayloadInvalid() throws Exception {
                mockMvc.perform(
                                post("/api/v1/posts")
                                                .with(authUser("user1"))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {"title":"","content":""}
                                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.fieldErrors.title").exists())
                                .andExpect(jsonPath("$.fieldErrors.content").exists());
        }

        @Test
        void update_returns200_whenAuthenticated() throws Exception {
                UUID id = UUID.randomUUID();
                when(postService.update(eq(id), any(), any())).thenReturn(
                                new PostPublicDTO(id, "t2", "c2", UUID.randomUUID(), "user1", 0, 0, Instant.now()));

                mockMvc.perform(
                                put("/api/v1/posts/{id}", id)
                                                .with(authUser("user1"))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {"title":"New title","content":"New content"}
                                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("t2"));
        }
}
