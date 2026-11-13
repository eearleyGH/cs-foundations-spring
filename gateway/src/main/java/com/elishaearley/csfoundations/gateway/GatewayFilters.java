package com.elishaearley.csfoundations.gateway;

import com.elishaearley.csfoundations.commons.jwt.JwtTokens;
import com.elishaearley.csfoundations.commons.trace.TraceIds;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayFilters extends OncePerRequestFilter {
    private final TokenBucketRateLimiter limiter;
    private final JwtTokens jwt;

    public GatewayFilters(TokenBucketRateLimiter limiter, JwtTokens jwt) {
        this.limiter = limiter;
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        String traceId = headerOrNew(request);
        response.setHeader(TraceIds.HEADER, traceId);

        String client = clientKey(request);
        if (!limiter.tryConsume(client)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "1");
            return;
        }

        if (requiresJwt(request)) {
            String token = bearer(request);
            if (token == null) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
            try {
                jwt.verify(token);
            } catch (IllegalArgumentException ex) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private static boolean requiresJwt(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/");
    }

    private static String bearer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring("Bearer ".length()).trim();
    }

    private static String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String headerOrNew(HttpServletRequest request) {
        String incoming = request.getHeader(TraceIds.HEADER);
        return incoming == null || incoming.isBlank() ? TraceIds.newId() : incoming;
    }
}
