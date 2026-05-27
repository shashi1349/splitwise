package com.shashi.splitwise.common.security;

/**
 * Authenticated principal extracted from a verified JWT.
 * Set as {@code Authentication#getPrincipal()} by {@link JwtAuthenticationFilter}
 * and consumed by controllers via {@code @AuthenticationPrincipal}.
 */
public record AuthenticatedUser(Long id, String email) {}
