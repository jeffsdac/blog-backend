package br.com.jeffsdac.blog.blog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.jeffsdac.blog.blog.model.userBlog.dto.LoginDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/user")
public class BlogUserController {

    @PostMapping("/login")
    public ResponseEntity<LoginDTO> doLogin(@RequestBody @Valid LoginDTO loginDTO) {
        if (loginDTO.username().equals("jeffin")) {
            return ResponseEntity.ok().body(loginDTO);
        }
        return ResponseEntity.status(403).body(loginDTO);
    }

}
