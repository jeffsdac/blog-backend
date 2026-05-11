package br.com.jeffsdac.blog.blog.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jeffsdac.blog.blog.model.posts.PostModel;

public interface PostRepository extends JpaRepository<PostModel, UUID> {
}

