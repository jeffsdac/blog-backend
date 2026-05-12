package br.com.jeffsdac.blog.blog.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.jeffsdac.blog.blog.model.postReaction.PostReaction;
import br.com.jeffsdac.blog.blog.model.postReaction.enums.ReactionType;

public interface PostReactionRepository extends JpaRepository<PostReaction, UUID> {

    @Query("""
                SELECT pr
                FROM PostReaction pr
                WHERE pr.postModel.id = :postId
                AND pr.userBlog.id = :userId
            """)
    Optional<PostReaction> findByPostIdAndUserId(@Param("postId") UUID postId, @Param("userId") UUID userId);

    @Query("""
            SELECT COUNT(pr)
            FROM PostReaction pr
            WHERE pr.postModel.id = :postId
            AND pr.type = :type
            """)
    long countByPostIdAndType(@Param("postId") UUID postId, @Param("type") ReactionType type);
}
