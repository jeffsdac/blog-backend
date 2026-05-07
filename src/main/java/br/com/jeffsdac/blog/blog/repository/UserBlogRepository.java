package br.com.jeffsdac.blog.blog.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;

public interface UserBlogRepository extends JpaRepository<UserBlog, UUID> {

    Optional<UserBlog> findByUsername(String username);

    Optional<UserBlog> findByEmail(String email);
}
