package br.com.jeffsdac.blog.blog.model.auths;

import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;

import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_BLOG_USER_ROLE_ASSIGNMENT")
public class UserRoleAssignmentModel implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserBlog user;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private RoleModel role;

    @Override
    public String getAuthority() {
        return this.role.getName();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserBlog getUser() {
        return user;
    }

    public void setUser(UserBlog user) {
        this.user = user;
    }

    public RoleModel getRole() {
        return role;
    }

    public void setRole(RoleModel role) {
        this.role = role;
    }

}
