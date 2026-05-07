package br.com.jeffsdac.blog.blog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.jeffsdac.blog.blog.exception.ConflictException;
import br.com.jeffsdac.blog.blog.exception.InvalidCredentialsException;
import br.com.jeffsdac.blog.blog.exception.ValidationException;
import br.com.jeffsdac.blog.blog.model.auths.dto.TokenDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.model.userBlog.dto.LoginDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.dto.RegisterUserDTO;
import br.com.jeffsdac.blog.blog.repository.UserBlogRepository;
import br.com.jeffsdac.blog.blog.service.TokenService;
import br.com.jeffsdac.blog.blog.service.UserBlogService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = BlogUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class BlogUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserBlogService userBlogService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserBlogRepository userBlogRepository;

    @Test
    void login_returnsToken_whenCredentialsAreValid() throws Exception {
        when(userBlogService.login(any(LoginDTO.class))).thenReturn(new TokenDTO("jwt-token"));

        mockMvc.perform(
                post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"usuario123","password":"password1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("jwt-token"));
    }

    @Test
    void login_returns403_whenCredentialsAreInvalid() throws Exception {
        when(userBlogService.login(any(LoginDTO.class)))
                .thenThrow(new InvalidCredentialsException("Usuário ou senha inválidos."));

        mockMvc.perform(
                post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"usuario123","password":"wrong-password"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Usuário ou senha inválidos."))
                .andExpect(jsonPath("$.path").value("/api/v1/user/login"));
    }

    @Test
    void login_returns400_whenPayloadIsInvalid() throws Exception {
        mockMvc.perform(
                post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void register_returns201_whenSuccessful() throws Exception {
        UserBlog saved = new UserBlog();
        saved.setId(java.util.UUID.randomUUID());
        saved.setUsername("usuario123");
        saved.setEmail("user@example.com");

        when(userBlogService.saveUser(any(RegisterUserDTO.class))).thenReturn(saved);

        mockMvc.perform(
                post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"usuario123",
                                  "email":"user@example.com",
                                  "password":"password1234",
                                  "confirmedPassword":"password1234",
                                  "firstName":"João",
                                  "lastName":"Silva"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("usuario123"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void register_returns409_whenUsernameAlreadyExists() throws Exception {
        when(userBlogService.saveUser(any(RegisterUserDTO.class)))
                .thenThrow(new ConflictException("Username já cadastrado."));

        mockMvc.perform(
                post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"usuario123",
                                  "email":"user@example.com",
                                  "password":"password1234",
                                  "confirmedPassword":"password1234"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username já cadastrado."))
                .andExpect(jsonPath("$.path").value("/api/v1/user/register"));
    }

    @Test
    void register_returns400_whenPasswordMismatch() throws Exception {
        when(userBlogService.saveUser(any(RegisterUserDTO.class)))
                .thenThrow(new ValidationException("As senhas precisam ser iguais."));

        mockMvc.perform(
                post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"usuario123",
                                  "email":"user@example.com",
                                  "password":"password1234",
                                  "confirmedPassword":"password12345"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("As senhas precisam ser iguais."));
    }

    @Test
    void register_returns400_whenPayloadIsInvalid() throws Exception {
        mockMvc.perform(
                post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","email":"not-an-email","password":"","confirmedPassword":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.confirmedPassword").exists());
    }
}
