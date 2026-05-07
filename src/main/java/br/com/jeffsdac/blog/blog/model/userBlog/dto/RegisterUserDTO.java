package br.com.jeffsdac.blog.blog.model.userBlog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserDTO(
        @NotBlank(message = "O username não pode estar em branco.") @Size(min = 5, max = 50, message = "O username fornecido possui o tamanho inválido.") String username,
        @NotBlank(message = "O email não pode estar em branco.") @Email(message = "Insira um formato válido de email.") String email,
        @NotBlank(message = "O password não pode estar em branco.") @Size(min = 10, max = 300, message = "A senha fornecida possui o tamanho inválido.") String password,
        @NotBlank(message = "A confirmação de senha não pode estar em branco.") @Size(min = 10, max = 300, message = "A confirmação de senha possui o tamanho inválido.") String confirmedPassword,
        String firstName,
        String lastName) {
}

