package com.shashi.splitwise.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed binding for {@code app.*} configuration.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors) {

    public record Jwt(String secret, long ttlMinutes, String issuer) {}

    public record Cors(String allowedOrigin) {}
}
