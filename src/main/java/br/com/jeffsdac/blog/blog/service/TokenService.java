package br.com.jeffsdac.blog.blog.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;

@Service
public class TokenService {

    // private static Logger log = LoggerFactory.getLogger(TokenService.class);

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(UserBlog userModel) {

        Algorithm algo = Algorithm.HMAC256(secret);

        return JWT.create()
                .withIssuer("chat-auth-api")
                .withSubject(userModel.getUsername())
                .withExpiresAt(getExpirationDate())
                .withClaim("userId", userModel.getId().toString())
                .sign(algo);

    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer("chat-auth-api")
                    .build()
                    .verify(token)
                    .getClaim("userId")
                    .asString();
        } catch (Exception e) {
            // log.error("Erro ao validar o token: " + e.getMessage());
            throw new RuntimeException("Invalid token: " + e.getMessage(), e);
        }
    }

    private Instant getExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

}