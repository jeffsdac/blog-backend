package br.com.jeffsdac.blog.blog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.com.jeffsdac.blog.blog.exception.NotFoundException;
import br.com.jeffsdac.blog.blog.model.postReaction.PostReaction;
import br.com.jeffsdac.blog.blog.model.postReaction.DTOs.TogglePostReaction;
import br.com.jeffsdac.blog.blog.model.postReaction.enums.ReactionType;
import br.com.jeffsdac.blog.blog.model.posts.PostModel;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.repository.PostReactionRepository;
import br.com.jeffsdac.blog.blog.repository.PostRepository;
import br.com.jeffsdac.blog.blog.repository.UserBlogRepository;
import jakarta.transaction.Transactional;

@Service
public class PostReactionService {

    private static final Logger log = LoggerFactory.getLogger(PostReactionService.class);

    private final PostReactionRepository postReactionRepository;
    private final PostRepository postRepository;
    private final UserBlogRepository userBlogRepository;

    public PostReactionService(
            PostReactionRepository postReactRepo,
            PostRepository postRepo,
            UserBlogRepository userBlogRepo) {
        this.postReactionRepository = postReactRepo;
        this.postRepository = postRepo;
        this.userBlogRepository = userBlogRepo;
    }

    @Transactional
    public TogglePostReaction react(TogglePostReaction dto) {
        log.info("Processing post reaction postId={} userId={} reaction={}",
                dto.postId(),
                dto.userId(),
                dto.reaction());

        PostModel post = postRepository.findById(dto.postId())
                .orElseThrow(() -> new NotFoundException("Não foi possível encontrar post com o ID fornecido"));

        UserBlog user = userBlogRepository.findById(dto.userId())
                .orElseThrow(() -> new NotFoundException("Não foi possível encontrar usuário com o ID fornecido"));

        postReactionRepository.findByPostIdAndUserId(dto.postId(), dto.userId())
                .ifPresentOrElse(
                        existingPostReaction -> handleExistingReaction(existingPostReaction, dto.reaction()),
                        () -> createReaction(user, post, dto.reaction()));

        log.info("Post reaction processed postId={} userId={} reaction={}",
                dto.postId(),
                dto.userId(),
                dto.reaction());

        return dto;
    }

    private void handleExistingReaction(PostReaction existingPostReactionModel, ReactionType type) {
        if (existingPostReactionModel.getType() == type) {
            log.info("Removing post reaction id={} postId={} userId={} reaction={}",
                    existingPostReactionModel.getId(),
                    existingPostReactionModel.getPostModel().getId(),
                    existingPostReactionModel.getUserBlog().getId(),
                    type);

            postReactionRepository.delete(existingPostReactionModel);
            return;
        }

        log.info("Updating post reaction id={} postId={} userId={} from={} to={}",
                existingPostReactionModel.getId(),
                existingPostReactionModel.getPostModel().getId(),
                existingPostReactionModel.getUserBlog().getId(),
                existingPostReactionModel.getType(),
                type);

        existingPostReactionModel.setType(type);
        postReactionRepository.save(existingPostReactionModel);

    }

    private void createReaction(UserBlog user, PostModel post, ReactionType type) {
        log.info("Creating post reaction postId={} userId={} reaction={}", post.getId(), user.getId(), type);

        PostReaction postReaction = new PostReaction(post, user, type);
        PostReaction savedReaction = postReactionRepository.save(postReaction);

        log.info("Post reaction created id={} postId={} userId={} reaction={}",
                savedReaction.getId(),
                post.getId(),
                user.getId(),
                type);
    }

}
