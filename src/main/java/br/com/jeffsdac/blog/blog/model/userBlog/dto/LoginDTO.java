package br.com.jeffsdac.blog.blog.model.userBlog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDTO(
        @NotBlank(message = "O username não pode estar em branco.") @Size(min = 5, max = 100, message = "O username fornecido possui o tamanho inválido.") String username,

        @NotBlank(message = "O password não pode estar em branco.") @Size(min = 10, max = 300, message = "A senha fornecida possui o tamanho inválido.") String password) {

}
