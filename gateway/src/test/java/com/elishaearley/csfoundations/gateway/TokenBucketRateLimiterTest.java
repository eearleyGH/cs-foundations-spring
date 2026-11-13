package com.elishaearley.csfoundations.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "app.rate-limit.capacity=3",
        "app.rate-limit.refill-per-second=0",
        "spring.cloud.gateway.server.webmvc.routes[0].id=noop",
        "spring.cloud.gateway.server.webmvc.routes[0].uri=http://127.0.0.1:9",
        "spring.cloud.gateway.server.webmvc.routes[0].predicates[0]=Path=/no-such-route"
})
@Testcontainers
class TokenBucketRateLimiterTest {
    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    TokenBucketRateLimiter limiter;

    @Test
    void sharedRedisBucketExhausts() {
        assertThat(limiter.tryConsume("it")).isTrue();
        assertThat(limiter.tryConsume("it")).isTrue();
        assertThat(limiter.tryConsume("it")).isTrue();
        assertThat(limiter.tryConsume("it")).isFalse();
    }
}
