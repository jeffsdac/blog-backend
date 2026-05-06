package br.com.jeffsdac.blog.blog.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jeffsdac.blog.blog.model.auths.UserRoleAssignmentModel;

public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignmentModel, UUID> {

}
