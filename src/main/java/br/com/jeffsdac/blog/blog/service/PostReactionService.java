package br.com.jeffsdac.blog.blog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public PostReactionResponseDTO react(TogglePostReaction dto, UserBlog authenticatedUser) {
        if (authenticatedUser == null) {
            throw new ForbiddenException("Authenticated user is required to react to a post.");
        }

        log.info("Processing post reaction postId={} userId={} reaction={}",
                dto.postId(),
                authenticatedUser.getId(),
                dto.reaction());

        PostModel post = postRepository.findById(dto.postId())
                .orElseThrow(() -> new NotFoundException("Nao foi possivel encontrar post com o ID fornecido"));

        UserBlog user = userBlogRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new NotFoundException("Nao foi possivel encontrar usuario com o ID fornecido"));

        PostReactionAction action = postReactionRepository.findByPostIdAndUserId(dto.postId(), user.getId())
                .map(existingPostReaction -> handleExistingReaction(existingPostReaction, dto.reaction()))
                .orElseGet(() -> createReaction(user, post, dto.reaction()));

        ReactionType currentReaction = action == PostReactionAction.REMOVED ? null : dto.reaction();
        long likeVotes = postReactionRepository.countByPostIdAndType(dto.postId(), ReactionType.LIKE);
        long deslikeVotes = postReactionRepository.countByPostIdAndType(dto.postId(), ReactionType.DESLIKE);

        log.info("Post reaction processed postId={} userId={} action={} currentReaction={} likeVotes={} deslikeVotes={}",
                dto.postId(),
                user.getId(),
                action,
                currentReaction,
                likeVotes,
                deslikeVotes);

        return new PostReactionResponseDTO(
                dto.postId(),
                user.getId(),
                currentReaction,
                action,
                likeVotes,
                deslikeVotes);
    }

    private PostReactionAction handleExistingReaction(PostReaction existingPostReactionModel, ReactionType type) {
        if (existingPostReactionModel.getType() == type) {
            log.info("Removing post reaction id={} postId={} userId={} reaction={}",
                    existingPostReactionModel.getId(),
                    existingPostReactionModel.getPostModel().getId(),
                    existingPostReactionModel.getUserBlog().getId(),
                    type);

            postReactionRepository.delete(existingPostReactionModel);
            return PostReactionAction.REMOVED;
        }

        log.info("Updating post reaction id={} postId={} userId={} from={} to={}",
                existingPostReactionModel.getId(),
                existingPostReactionModel.getPostModel().getId(),
                existingPostReactionModel.getUserBlog().getId(),
                existingPostReactionModel.getType(),
                type);

        existingPostReactionModel.setType(type);
        postReactionRepository.save(existingPostReactionModel);
        return PostReactionAction.UPDATED;
    }

    private PostReactionAction createReaction(UserBlog user, PostModel post, ReactionType type) {
        log.info("Creating post reaction postId={} userId={} reaction={}", post.getId(), user.getId(), type);

        PostReaction postReaction = new PostReaction(post, user, type);
        PostReaction savedReaction = postReactionRepository.save(postReaction);

        log.info("Post reaction created id={} postId={} userId={} reaction={}",
                savedReaction.getId(),
                post.getId(),
                user.getId(),
                type);

        return PostReactionAction.CREATED;
    }
}
