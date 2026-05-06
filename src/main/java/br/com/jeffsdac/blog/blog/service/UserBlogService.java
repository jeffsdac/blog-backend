package br.com.jeffsdac.blog.blog.service;

import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.jeffsdac.blog.blog.model.auths.UserRoleAssignmentModel;
import br.com.jeffsdac.blog.blog.model.auths.dto.TokenDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.model.userBlog.dto.LoginDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.dto.RegisterUserDTO;
import br.com.jeffsdac.blog.blog.repository.RoleRepository;
import br.com.jeffsdac.blog.blog.repository.UserBlogRepository;
import br.com.jeffsdac.blog.blog.repository.UserRoleAssignmentRepository;
import jakarta.transaction.Transactional;

public class UserBlogService {

    private TokenService tokenService;
    private UserBlogRepository userRepo;
    private PasswordEncoder passwordEncoder;
    private RoleRepository roleRepository;
    private UserRoleAssignmentRepository userRoleRepo;
    // private static final Logger log =
    // LoggerFactory.getLogger(CommomUserService.class);

    public UserBlogService(UserBlogRepository userRepository,
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

        // log.info("EM USER SERVICE COM O USUARIODTO: " + userDto.username());
        var role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("NÃO FOI ENCONTRADO O ROLE_USER"));
        // log.info("ROLES FORAM ENCONTRADAS");

        var user = new UserBlog();
        user.setEmail(userDto.email());
        user.setUsername(userDto.username());
        String encryptedPassword = passwordEncoder.encode(userDto.password());
        user.setPassword(encryptedPassword);
        var savedUser = userRepo.save(user);
        // log.info("USUARIO SALVO ID: " + savedUser.getId());

        var assignment = new UserRoleAssignmentModel();
        assignment.setRole(role);
        assignment.setUser(savedUser);
        var savedAssignment = userRoleRepo.save(assignment);
        // log.info("RELACAO COM ROLE: " + savedAssignment.getRole().getName() + " E
        // USUARIO: "
        // + savedAssignment.getUser().getUsername() + " FEITA");

        return savedUser;
    }

    @Transactional
    public TokenDTO login(LoginDTO loginDTO) throws RuntimeException {
        // log.info("In Service user, with user: " + loginDTO);

        UserBlog userModel = userRepo.findByUsername(loginDTO.username())
                .orElseThrow(() -> new RuntimeException());
        // log.info("Found user by username: " + loginDTO);

        boolean passwordIsCorrect = passwordEncoder.matches(loginDTO.password(), userModel.getPassword());
        if (!passwordIsCorrect)
            throw new RuntimeException();
        // log.info("Correct password");

        String token = tokenService.generateToken(userModel);
        // log.info("Sucessfull generate token");
        return new TokenDTO(token);

    }

}
