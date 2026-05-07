package br.com.jeffsdac.blog.blog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.jeffsdac.blog.blog.model.auths.dto.TokenDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.dto.LoginDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.dto.RegisterUserDTO;
import br.com.jeffsdac.blog.blog.model.userBlog.dto.UserBlogPublicDTO;
import br.com.jeffsdac.blog.blog.service.UserBlogService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/user")
public class BlogUserController {

    private final UserBlogService userBlogService;

    public BlogUserController(UserBlogService userBlogService) {
        this.userBlogService = userBlogService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> doLogin(@RequestBody @Valid LoginDTO loginDTO) {
        return ResponseEntity.ok(userBlogService.login(loginDTO));
    }

    @PostMapping("/register")
    public ResponseEntity<UserBlogPublicDTO> register(@RequestBody @Valid RegisterUserDTO registerUserDTO) {
        var user = userBlogService.saveUser(registerUserDTO);
        var dto = new UserBlogPublicDTO(user.getId(), user.getUsername(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
