package br.com.jeffsdac.blog.blog.config;

import java.io.IOException;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import br.com.jeffsdac.blog.blog.repository.UserBlogRepository;
import br.com.jeffsdac.blog.blog.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

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

        try {

            String userId = tokenService.validateToken(token);
            UserBlog user = commomUserRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("User not found in the filter"));

            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private String recoveryToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }

}
