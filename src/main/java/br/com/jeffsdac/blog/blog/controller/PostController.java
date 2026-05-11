package br.com.jeffsdac.blog.blog.controller;

import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.jeffsdac.blog.blog.model.genericDtos.PageResponseDTO;
import br.com.jeffsdac.blog.blog.model.posts.dto.CreatePostDTO;
import br.com.jeffsdac.blog.blog.model.posts.dto.PostPublicDTO;
import br.com.jeffsdac.blog.blog.model.posts.dto.UpdatePostDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.service.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/posts")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostPublicDTO> getById(@PathVariable UUID id) {
        log.debug("GET /api/v1/posts/{}", id);
        return ResponseEntity.ok(postService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<PostPublicDTO>> getAll(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        log.debug("GET /api/v1/post limit={} offset={}", limit, offset);
        return ResponseEntity.ok(postService.getAll(limit, offset));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<PostPublicDTO> create(
            @AuthenticationPrincipal UserBlog author,
            @RequestBody @Valid CreatePostDTO dto) {
        log.debug("POST /api/v1/posts by author={}", author == null ? "null" : author.getUsername());
        PostPublicDTO created = postService.create(dto, author);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/v1/posts/" + created.id()))
                .body(created);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<PostPublicDTO> update(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserBlog actor,
            @RequestBody @Valid UpdatePostDTO dto) {
        log.debug("PUT /api/v1/posts/{} by actor={}", id, actor == null ? "null" : actor.getUsername());
        return ResponseEntity.ok(postService.update(id, dto, actor));
    }
}
