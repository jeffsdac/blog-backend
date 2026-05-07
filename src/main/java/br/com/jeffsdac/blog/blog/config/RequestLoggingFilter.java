package br.com.jeffsdac.blog.blog.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        long startNanos = System.nanoTime();
        StatusCapturingResponseWrapper responseWrapper = new StatusCapturingResponseWrapper(response);
        String requestId = resolveRequestId(request);
        MDC.put("requestId", requestId);

        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String fullPath = query == null ? path : (path + "?" + query);

        String userAgent = request.getHeader("User-Agent");
        String contentType = request.getContentType();
        String remoteAddr = request.getRemoteAddr();
        log.debug("Incoming request {} {} (remote={} contentType={} userAgent={})", method, fullPath, remoteAddr,
                contentType, userAgent);

        try {
            filterChain.doFilter(request, responseWrapper);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            int status = responseWrapper.getStatus();
            log.info("{} {} -> {} ({}ms)", method, fullPath, status, durationMs);
            MDC.remove("requestId");
        }
    }

    private static String resolveRequestId(HttpServletRequest request) {
        String header = request.getHeader("X-Request-Id");
        if (header != null && !header.isBlank()) {
            return header;
        }
        return UUID.randomUUID().toString();
    }

    private static final class StatusCapturingResponseWrapper extends HttpServletResponseWrapper {
        private int httpStatus = HttpServletResponse.SC_OK;

        private StatusCapturingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            this.httpStatus = sc;
            super.setStatus(sc);
        }

        @Override
        public void sendError(int sc) throws IOException {
            this.httpStatus = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.httpStatus = sc;
            super.sendError(sc, msg);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            this.httpStatus = HttpServletResponse.SC_FOUND;
            super.sendRedirect(location);
        }

        @Override
        public int getStatus() {
            return this.httpStatus;
        }
    }
}
