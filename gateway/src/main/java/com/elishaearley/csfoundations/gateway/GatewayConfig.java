package com.elishaearley.csfoundations.gateway;

import com.elishaearley.csfoundations.commons.jwt.JwtTokens;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayConfig {
    @Bean
    JwtTokens jwtTokens(GatewayProperties properties) {
        GatewayProperties.Jwt jwt = properties.jwt();
        return new JwtTokens(jwt.secret(), jwt.issuer(), jwt.ttlSeconds());
    }
}
