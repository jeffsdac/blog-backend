package br.com.jeffsdac.blog.blog.model.common.dto;

import java.time.Instant;
import java.util.Map;

public record ApiErrorDTO(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors) {
}

