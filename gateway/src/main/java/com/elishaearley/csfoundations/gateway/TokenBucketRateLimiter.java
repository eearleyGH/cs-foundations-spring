package com.elishaearley.csfoundations.gateway;

import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class TokenBucketRateLimiter {
    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>();

    static {
        SCRIPT.setResultType(Long.class);
        SCRIPT.setScriptText("""
                local key = KEYS[1]
                local capacity = tonumber(ARGV[1])
                local refill = tonumber(ARGV[2])
                local now = tonumber(ARGV[3])
                local data = redis.call('HMGET', key, 'tokens', 'ts')
                local tokens = tonumber(data[1])
                local ts = tonumber(data[2])
                if tokens == nil then
                  tokens = capacity
                  ts = now
                end
                local elapsed = math.max(0, now - ts) / 1000.0
                tokens = math.min(capacity, tokens + elapsed * refill)
                local allowed = 0
                if tokens >= 1 then
                  tokens = tokens - 1
                  allowed = 1
                end
                redis.call('HSET', key, 'tokens', tokens, 'ts', now)
                redis.call('EXPIRE', key, 120)
                return allowed
                """);
    }

    private final StringRedisTemplate redis;
    private final GatewayProperties properties;

    public TokenBucketRateLimiter(StringRedisTemplate redis, GatewayProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    public boolean tryConsume(String clientKey) {
        GatewayProperties.RateLimit limit = properties.rateLimit();
        Long allowed = redis.execute(
                SCRIPT,
                List.of("tb:" + clientKey),
                String.valueOf(limit.capacity()),
                String.valueOf(limit.refillPerSecond()),
                String.valueOf(System.currentTimeMillis()));
        return allowed != null && allowed == 1L;
    }
}
