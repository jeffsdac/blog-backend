package br.com.jeffsdac.blog.blog.model.genericDtos;

import java.util.List;

public record PageResponseDTO<T>(
        List<T> items,
        int limit,
        int offset,
        long total) {

}
