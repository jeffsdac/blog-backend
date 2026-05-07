package br.com.jeffsdac.blog.blog.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.jeffsdac.blog.blog.model.auths.RoleModel;
import br.com.jeffsdac.blog.blog.model.auths.UserRoleAssignmentModel;
import br.com.jeffsdac.blog.blog.model.auths.dto.TokenDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.model.userBlog.dto.LoginDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.dto.RegisterUserDTO;
import br.com.jeffsdac.blog.blog.repository.RoleRepository;
import br.com.jeffsdac.blog.blog.repository.UserBlogRepository;
import br.com.jeffsdac.blog.blog.repository.UserRoleAssignmentRepository;
import jakarta.transaction.Transactional;

@Service
public class UserBlogService {

    private final TokenService tokenService;
    private final UserBlogRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository userRoleRepo;

    public UserBlogService(
            UserBlogRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository userRoleRepo,
            TokenService tokenService) {

        this.userRepo = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRoleRepo = userRoleRepo;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserBlog saveUser(RegisterUserDTO userDto) {

        if (userRepo.findByUsername(userDto.username()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepo.findByEmail(userDto.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (!userDto.password().equals(userDto.confirmedPassword())) {
            throw new RuntimeException("Password mismatch");
        }

        var role = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    RoleModel newRole = new RoleModel();
                    newRole.setName("ROLE_USER");
                    return roleRepository.save(newRole);
                });

        var user = new UserBlog();
        user.setEmail(userDto.email());
        user.setUsername(userDto.username());
        user.setFirstName(userDto.firstName() == null ? "" : userDto.firstName());
        user.setLastName(userDto.lastName() == null ? "" : userDto.lastName());

        String encryptedPassword = passwordEncoder.encode(userDto.password());
        user.setPassword(encryptedPassword);

        var savedUser = userRepo.save(user);

        var assignment = new UserRoleAssignmentModel();
        assignment.setRole(role);
        assignment.setUser(savedUser);
        userRoleRepo.save(assignment);

        return savedUser;
    }

    @Transactional
    public TokenDTO login(LoginDTO loginDTO) {

        UserBlog userModel = userRepo.findByUsername(loginDTO.username())
                .orElseThrow(RuntimeException::new);

        boolean passwordIsCorrect = passwordEncoder.matches(loginDTO.password(), userModel.getPassword());
        if (!passwordIsCorrect) {
            throw new RuntimeException();
        }

        String token = tokenService.generateToken(userModel);
        return new TokenDTO(token);
    }
}

