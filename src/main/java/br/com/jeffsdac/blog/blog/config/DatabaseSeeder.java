package br.com.jeffsdac.blog.blog.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.jeffsdac.blog.blog.model.auths.RoleModel;
import br.com.jeffsdac.blog.blog.model.auths.UserRoleAssignmentModel;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.repository.RoleRepository;
import br.com.jeffsdac.blog.blog.repository.UserBlogRepository;
import br.com.jeffsdac.blog.blog.repository.UserRoleAssignmentRepository;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

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

        RoleModel roleUser = new RoleModel();
        roleUser.setName("ROLE_USER");
        roleUser = roleRepository.save(roleUser);
        log.info("Created role: {}", roleUser.getName());

        RoleModel roleAdmin = new RoleModel();
        roleAdmin.setName("ROLE_ADMIN");
        roleAdmin = roleRepository.save(roleAdmin);
        log.info("Created role: {}", roleAdmin.getName());

        UserBlog user = new UserBlog();
        user.setUsername("jeffsdac");
        user.setEmail("user@example.com");
        user.setFirstName("User");
        user.setLastName("Example");
        user.setPassword(passwordEncoder.encode("user1234567890"));
        user = userBlogRepository.save(user);
        log.info("Created user: {} ({})", user.getUsername(), user.getEmail());

        UserRoleAssignmentModel userAssignment = new UserRoleAssignmentModel();
        userAssignment.setUser(user);
        userAssignment.setRole(roleUser);
        userRoleAssignmentRepository.save(userAssignment);
        log.info("Assigned role {} to user {}", roleUser.getName(), user.getUsername());

        UserBlog admin = new UserBlog();
        admin.setUsername("adminjeff");
        admin.setEmail("admin@example.com");
        admin.setFirstName("Admin");
        admin.setLastName("Example");
        admin.setPassword(passwordEncoder.encode("admin1234567890"));
        admin = userBlogRepository.save(admin);
        log.info("Created admin: {} ({})", admin.getUsername(), admin.getEmail());

        UserRoleAssignmentModel adminAssignment = new UserRoleAssignmentModel();
        adminAssignment.setUser(admin);
        adminAssignment.setRole(roleAdmin);
        userRoleAssignmentRepository.save(adminAssignment);
        log.info("Assigned role {} to user {}", roleAdmin.getName(), admin.getUsername());
    }
}

