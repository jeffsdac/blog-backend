package br.com.jeffsdac.blog.blog.config;

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

        RoleModel roleUser = new RoleModel();
        roleUser.setName("ROLE_USER");
        roleUser = roleRepository.save(roleUser);
        System.out.println("[DatabaseSeeder] Created role: " + roleUser.getName());

        RoleModel roleAdmin = new RoleModel();
        roleAdmin.setName("ROLE_ADMIN");
        roleAdmin = roleRepository.save(roleAdmin);
        System.out.println("[DatabaseSeeder] Created role: " + roleAdmin.getName());

        UserBlog user = new UserBlog();
        user.setUsername("jeffsdac");
        user.setEmail("user@example.com");
        user.setFirstName("User");
        user.setLastName("Example");
        user.setPassword(passwordEncoder.encode("user1234567890"));
        user = userBlogRepository.save(user);
        System.out.println("[DatabaseSeeder] Created user: " + user.getUsername() + " (" + user.getEmail() + ")");

        UserRoleAssignmentModel userAssignment = new UserRoleAssignmentModel();
        userAssignment.setUser(user);
        userAssignment.setRole(roleUser);
        userRoleAssignmentRepository.save(userAssignment);
        System.out.println("[DatabaseSeeder] Assigned role " + roleUser.getName() + " to user " + user.getUsername());

        UserBlog admin = new UserBlog();
        admin.setUsername("adminjeff");
        admin.setEmail("admin@example.com");
        admin.setFirstName("Admin");
        admin.setLastName("Example");
        admin.setPassword(passwordEncoder.encode("admin1234567890"));
        admin = userBlogRepository.save(admin);
        System.out.println("[DatabaseSeeder] Created admin: " + admin.getUsername() + " (" + admin.getEmail() + ")");

        UserRoleAssignmentModel adminAssignment = new UserRoleAssignmentModel();
        adminAssignment.setUser(admin);
        adminAssignment.setRole(roleAdmin);
        userRoleAssignmentRepository.save(adminAssignment);
        System.out.println("[DatabaseSeeder] Assigned role " + roleAdmin.getName() + " to user " + admin.getUsername());
    }
}


