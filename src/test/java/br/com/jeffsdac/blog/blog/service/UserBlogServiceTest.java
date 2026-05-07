package br.com.jeffsdac.blog.blog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.jeffsdac.blog.blog.exception.ConflictException;
import br.com.jeffsdac.blog.blog.exception.InvalidCredentialsException;
import br.com.jeffsdac.blog.blog.exception.ValidationException;
import br.com.jeffsdac.blog.blog.model.auths.RoleModel;
import br.com.jeffsdac.blog.blog.model.auths.dto.TokenDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.model.userBlog.dto.LoginDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.dto.RegisterUserDTO;
import br.com.jeffsdac.blog.blog.repository.RoleRepository;
import br.com.jeffsdac.blog.blog.repository.UserBlogRepository;
import br.com.jeffsdac.blog.blog.repository.UserRoleAssignmentRepository;

@ExtendWith(MockitoExtension.class)
class UserBlogServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserBlogRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleRepo;

    @InjectMocks
    private UserBlogService userBlogService;

    @Captor
    private ArgumentCaptor<UserBlog> userCaptor;

    @Captor
    private ArgumentCaptor<RoleModel> roleCaptor;

    @Test
    void saveUser_throws409_whenUsernameAlreadyExists() {
        when(userRepo.findByUsername("usuario123")).thenReturn(Optional.of(new UserBlog()));

        var dto = new RegisterUserDTO(
                "usuario123",
                "user@example.com",
                "password1234",
                "password1234",
                null,
                null);

        assertThrows(ConflictException.class, () -> userBlogService.saveUser(dto));
        verify(userRepo, never()).save(any());
    }

    @Test
    void saveUser_throws409_whenEmailAlreadyExists() {
        when(userRepo.findByUsername("usuario123")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.of(new UserBlog()));

        var dto = new RegisterUserDTO(
                "usuario123",
                "user@example.com",
                "password1234",
                "password1234",
                null,
                null);

        assertThrows(ConflictException.class, () -> userBlogService.saveUser(dto));
        verify(userRepo, never()).save(any());
    }

    @Test
    void saveUser_throws400_whenPasswordsDoNotMatch() {
        when(userRepo.findByUsername("usuario123")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.empty());

        var dto = new RegisterUserDTO(
                "usuario123",
                "user@example.com",
                "password1234",
                "password12345",
                null,
                null);

        assertThrows(ValidationException.class, () -> userBlogService.saveUser(dto));
        verify(userRepo, never()).save(any());
    }

    @Test
    void saveUser_throws400_whenRoleUserDoesNotExist() {
        when(userRepo.findByUsername("usuario123")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.empty());

        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        var dto = new RegisterUserDTO(
                "usuario123",
                "user@example.com",
                "password1234",
                "password1234",
                null,
                null);

        assertThrows(ValidationException.class, () -> userBlogService.saveUser(dto));
        verify(roleRepository, never()).save(any(RoleModel.class));
    }

    @Test
    void saveUser_setsDefaultNames_whenNull() {
        when(userRepo.findByUsername("usuario123")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.empty());

        RoleModel role = new RoleModel();
        role.setName("ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));

        when(passwordEncoder.encode("password1234")).thenReturn("hashed");
        when(userRepo.save(any(UserBlog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var dto = new RegisterUserDTO(
                "usuario123",
                "user@example.com",
                "password1234",
                "password1234",
                null,
                null);

        userBlogService.saveUser(dto);

        verify(userRepo).save(userCaptor.capture());
        assertEquals("", userCaptor.getValue().getFirstName());
        assertEquals("", userCaptor.getValue().getLastName());
    }

    @Test
    void login_throws403_whenUserDoesNotExist() {
        when(userRepo.findByUsername("usuario123")).thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> userBlogService.login(new LoginDTO("usuario123", "password1234")));
    }

    @Test
    void login_throws403_whenPasswordDoesNotMatch() {
        UserBlog user = new UserBlog();
        user.setPassword("hashed");
        when(userRepo.findByUsername("usuario123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("wrong"), eq("hashed"))).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> userBlogService.login(new LoginDTO("usuario123", "wrong")));
    }

    @Test
    void login_returnsToken_whenValid() {
        UserBlog user = new UserBlog();
        user.setPassword("hashed");
        when(userRepo.findByUsername("usuario123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("password1234"), eq("hashed"))).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("jwt");

        TokenDTO token = userBlogService.login(new LoginDTO("usuario123", "password1234"));
        assertEquals("jwt", token.value());
    }
}
