package br.com.jeffsdac.blog.blog.config;

import java.util.Objects;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.jeffsdac.blog.blog.model.auths.RoleModel;
import br.com.jeffsdac.blog.blog.model.auths.UserRoleAssignmentModel;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.repository.RoleRepository;
import br.com.jeffsdac.blog.blog.repository.UserBlogRepository;
import br.com.jeffsdac.blog.blog.repository.UserRoleAssignmentRepository;

@Component
@ConditionalOnBean({ RoleRepository.class, UserBlogRepository.class, UserRoleAssignmentRepository.class,
        PasswordEncoder.class })
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserBlogRepository userBlogRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(
            RoleRepository roleRepository,
            UserBlogRepository userBlogRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userBlogRepository = userBlogRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        RoleModel roleUser = ensureRoleExists("ROLE_USER");
        RoleModel roleAdmin = ensureRoleExists("ROLE_ADMIN");

        UserBlog user = ensureUserExists(
                "jeffsdac",
                "user@example.com",
                "User",
                "Example",
                "user1234567890");
        ensureUserHasRole(user, roleUser);

        UserBlog admin = ensureUserExists(
                "adminjeff",
                "admin@example.com",
                "Admin",
                "Example",
                "admin1234567890");
        ensureUserHasRole(admin, roleAdmin);
    }

    private RoleModel ensureRoleExists(String roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            RoleModel role = new RoleModel();
            role.setName(roleName);
            return roleRepository.save(role);
        });
    }

    private UserBlog ensureUserExists(
            String username,
            String email,
            String firstName,
            String lastName,
            String rawPassword) {

        return userBlogRepository.findByUsername(username).orElseGet(() -> {
            UserBlog user = new UserBlog();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(passwordEncoder.encode(rawPassword));
            return userBlogRepository.save(user);
        });
    }

    private void ensureUserHasRole(UserBlog user, RoleModel role) {
        boolean alreadyAssigned = user.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(a -> Objects.equals(a, role.getName()));

        if (alreadyAssigned) {
            return;
        }

        UserRoleAssignmentModel assignment = new UserRoleAssignmentModel();
        assignment.setUser(user);
        assignment.setRole(role);
        userRoleAssignmentRepository.save(assignment);
    }
}
