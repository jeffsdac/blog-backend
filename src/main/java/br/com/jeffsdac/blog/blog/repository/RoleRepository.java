package br.com.jeffsdac.blog.blog.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jeffsdac.blog.blog.model.auths.RoleModel;

public interface RoleRepository extends JpaRepository<RoleModel, UUID> {

    Optional<RoleModel> findByName(String name);
}
