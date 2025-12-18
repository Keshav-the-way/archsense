package com.archsense.gateway.util;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteValidator {

    private static final List<String> PUBLIC_ROUTES = List.of(
            "/api/users/register",
            "/api/users/forgot-password",
            "/api/users/reset-password",
            "/api/users/login",
            "/actuator/health",
            "/swagger-ui",
            "/swagger-ui.html",
            "/swagger-ui/",
            "/v3/api-docs",
            "/api-docs",
            "/swagger-resources",
            "/webjars/"
    );

    public boolean isPublicRoute(String path, String method) {
        return PUBLIC_ROUTES.stream()
                .anyMatch(route -> path.equals(route) || path.startsWith(route));
    }
}