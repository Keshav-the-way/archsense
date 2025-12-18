package com.archsense.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "rate-limit.enabled", havingValue = "true")
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private final RedisTemplate<String, String> redisTemplate;
    private final int requestsPerMinute;

    public RateLimitFilter(RedisTemplate<String, String> redisTemplate,
                           @Value("${rate-limit.requests-per-minute}") int requestsPerMinute) {
        this.redisTemplate = redisTemplate;
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = (String) request.getAttribute("X-User-Id");
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = RATE_LIMIT_PREFIX + userId;

        Long currentCount = redisTemplate.opsForValue().increment(key);
        if (currentCount == null) {
            currentCount = 0L;
        }

        if (currentCount == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }

        if (currentCount > requestsPerMinute) {
            log.warn("Rate limit exceeded for user: {}", userId);
            response.setStatus(429);
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
            response.setContentType("application/json");
            return;
        }

        response.setHeader("X-Rate-Limit-Limit", String.valueOf(requestsPerMinute));
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(requestsPerMinute - currentCount));

        filterChain.doFilter(request, response);
    }
}