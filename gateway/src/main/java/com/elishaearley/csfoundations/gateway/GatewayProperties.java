package com.elishaearley.csfoundations.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record GatewayProperties(Jwt jwt, RateLimit rateLimit) {
    public record Jwt(String secret, String issuer, long ttlSeconds) {}

    public record RateLimit(int capacity, double refillPerSecond) {}
}
