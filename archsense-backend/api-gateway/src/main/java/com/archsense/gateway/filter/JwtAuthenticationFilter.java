package com.archsense.gateway.filter;

import com.archsense.common.exception.UnauthorizedException;
import com.archsense.common.util.JwtUtil;
import com.archsense.gateway.util.RouteValidator;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String JWT_CACHE_PREFIX = "jwt:";

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   RouteValidator routeValidator,
                                   RedisTemplate<String, String> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip documentation-only endpoints (they throw UnsupportedOperationException)
        if ("OPTIONS".equals(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (routeValidator.isPublicRoute(path, method)) {
            log.debug("Public route accessed: {} {}", method, path);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or invalid Authorization header for: {} {}", method, path);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            String userId = validateTokenAndGetUserId(token);
            request.setAttribute("X-User-Id", userId);
            log.debug("Authenticated user: {} for {} {}", userId, method, path);

            filterChain.doFilter(request, response);

        } catch (UnauthorizedException e) {
            log.warn("Invalid token for: {} {}", method, path);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            log.error("Authentication error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error");
        }
    }

    private String validateTokenAndGetUserId(String token) {
        String cacheKey = JWT_CACHE_PREFIX + token;

        String cachedUserId = redisTemplate.opsForValue().get(cacheKey);
        if (cachedUserId != null) {
            log.debug("JWT validated from cache");
            return cachedUserId;
        }

        try {
            Claims claims = jwtUtil.validateAndExtract(token);
            String userId = claims.getSubject();

            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(cacheKey, userId, ttl, TimeUnit.MILLISECONDS);
                log.debug("JWT cached for user: {}", userId);
            }

            return userId;

        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }
}