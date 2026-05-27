package com.shashi.splitwise.common.api;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * JSON-friendly wrapper around {@link Page} that hides Spring's internal
 * {@code Pageable} / {@code Sort} structure from API consumers.
 */
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext
) {

    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(
            p.getContent(),
            p.getNumber(),
            p.getSize(),
            p.getTotalElements(),
            p.getTotalPages(),
            p.hasNext()
        );
    }
}
