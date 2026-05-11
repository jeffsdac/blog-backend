package br.com.jeffsdac.blog.blog.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.com.jeffsdac.blog.blog.exception.ForbiddenException;
import br.com.jeffsdac.blog.blog.exception.NotFoundException;
import br.com.jeffsdac.blog.blog.exception.ValidationException;
import br.com.jeffsdac.blog.blog.model.genericDtos.PageResponseDTO;
import br.com.jeffsdac.blog.blog.model.posts.PostModel;
import br.com.jeffsdac.blog.blog.model.posts.dto.CreatePostDTO;
import br.com.jeffsdac.blog.blog.model.posts.dto.PostPublicDTO;
import br.com.jeffsdac.blog.blog.model.posts.dto.UpdatePostDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.repository.PostRepository;
import jakarta.transaction.Transactional;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostPublicDTO getById(UUID id) {
        log.debug("Fetching post by id={}", id);
        var post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found."));
        return toPublicDto(post);
    }

    @Transactional
    public PostPublicDTO create(CreatePostDTO dto, UserBlog author) {
        log.info("Creating post for author={}", author.getUsername());
        PostModel post = new PostModel();
        post.setTitle(dto.title());
        post.setContent(dto.content());
        post.setAuthor(author);
        PostModel saved = postRepository.save(post);
        log.info("Post created id={} author={}", saved.getId(), author.getUsername());
        return toPublicDto(saved);
    }

    @Transactional
    public PostPublicDTO update(UUID id, UpdatePostDTO dto, UserBlog actor) {
        log.info("Updating post id={} by actor={}", id, actor.getUsername());
        PostModel post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found."));
        ensureCanEdit(post, actor);
        post.setTitle(dto.title());
        post.setContent(dto.content());
        PostModel saved = postRepository.save(post);
        log.info("Post updated id={} by actor={}", id, actor.getUsername());
        return toPublicDto(saved);
    }

    public PageResponseDTO<PostPublicDTO> getAll(int limit, int offset) {
        if (limit < 1 || limit > 20) {
            throw new ValidationException("Limit must be between 1 and 20.");
        }
        log.debug("Fetching posts limit={} offset={}", limit, offset);
        List<PostPublicDTO> posts = postRepository.findAllOrderedByCreatedAtDesc(limit, offset)
                .stream()
                .map(this::toPublicDto)
                .toList();

        long total = postRepository.count();

        return new PageResponseDTO<>(posts, limit, offset, total);
    }

    private void ensureCanEdit(PostModel post, UserBlog actor) {
        UUID authorId = post.getAuthor().getId();
        if (authorId.equals(actor.getId())) {
            return;
        }
        throw new ForbiddenException("You cannot modify this post.");
    }

    private PostPublicDTO toPublicDto(PostModel post) {
        return new PostPublicDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor() == null ? null : post.getAuthor().getId(),
                post.getAuthor() == null ? null : post.getAuthor().getUsername(),
                post.getLikeVotes(),
                post.getUnlikeVotes(),
                post.getCreatedAt());
    }
}
