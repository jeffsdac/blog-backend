package br.com.jeffsdac.blog.blog.config;

import java.io.IOException;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.repository.UserBlogRepository;
import br.com.jeffsdac.blog.blog.exception.InvalidTokenException;
import br.com.jeffsdac.blog.blog.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    private TokenService tokenService;
    private UserBlogRepository commomUserRepository;

    public AuthenticationFilter(TokenService tokenService, UserBlogRepository userBlogRepository) {
        this.tokenService = tokenService;
        this.commomUserRepository = userBlogRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        var token = recoveryToken(request);

        if (token == null || token.isBlank()) {
            log.debug("No bearer token present for {} {}. Skipping auth.", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            log.debug("Bearer token present for {} {}. Validating token...", request.getMethod(), request.getRequestURI());
            String userId = tokenService.validateToken(token);
            log.debug("Token valid. Resolving userId={}...", userId);
            UserBlog user = commomUserRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new InvalidTokenException("Token inválido."));
            log.debug("User resolved. Setting SecurityContext for username={}", user.getUsername());
            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (InvalidTokenException ex) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            log.debug("Invalid token for request {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String recoveryToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        if (!authHeader.startsWith("Bearer ")) {
            log.debug("Authorization header is present but not Bearer for {} {}.", request.getMethod(),
                    request.getRequestURI());
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }

}
