package br.com.jeffsdac.blog.blog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.jeffsdac.blog.blog.exception.InvalidTokenException;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;

class TokenServiceTest {

    @Test
    void generateToken_andValidateToken_roundTripReturnsUserId() {
        TokenService tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "unit-test-secret");

        UserBlog user = new UserBlog();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setUsername("usuario123");

        String token = tokenService.generateToken(user);
        assertNotNull(token);

        String userId = tokenService.validateToken(token);
        assertEquals(id.toString(), userId);
    }

    @Test
    void validateToken_throws_whenTokenIsInvalid() {
        TokenService tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "unit-test-secret");

        assertThrows(InvalidTokenException.class, () -> tokenService.validateToken("not-a-jwt"));
    }
}
