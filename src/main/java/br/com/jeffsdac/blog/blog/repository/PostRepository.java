package br.com.jeffsdac.blog.blog.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.jeffsdac.blog.blog.model.posts.PostModel;

public interface PostRepository extends JpaRepository<PostModel, UUID> {

    @Query(value = """
            SELECT *
            FROM T_BLOG_POST
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<PostModel> findAllOrderedByCreatedAtDesc(
            @Param("limit") int limit,
            @Param("offset") int offset);

}
