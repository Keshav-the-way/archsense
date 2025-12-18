package com.archsense.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ProxyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ProxyFilter.class);

    private final String userServiceUrl;
    private final String projectServiceUrl;
    private final String artifactServiceUrl;
    private final String analysisServiceUrl;

    public ProxyFilter(@Value("${services.user-service.base-url}") String userServiceUrl,
                       @Value("${services.project-service.base-url}") String projectServiceUrl,
                       @Value("${services.artifact-service.base-url}") String artifactServiceUrl,
                       @Value("${services.analysis-service.base-url}") String analysisServiceUrl) {
        this.userServiceUrl = userServiceUrl;
        this.projectServiceUrl = projectServiceUrl;
        this.artifactServiceUrl = artifactServiceUrl;
        this.analysisServiceUrl = analysisServiceUrl;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String contentType = request.getContentType();

        // Skip proxy filter for multipart requests - they need special handling
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            log.debug("Skipping ProxyFilter for multipart request: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // For non-proxied paths, continue the filter chain
        String targetUrl = determineTargetUrl(path);
        if (targetUrl == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Log the proxying (actual proxying happens in controller via Feign/RestTemplate)
        log.debug("Request will be proxied: {} {} -> {}", request.getMethod(), path, targetUrl);

        // Continue filter chain - actual proxying happens at controller level
        filterChain.doFilter(request, response);
    }

    private String determineTargetUrl(String path) {
        if (path.startsWith("/api/users")) {
            return userServiceUrl;
        } else if (path.startsWith("/api/analyses")) {
            return analysisServiceUrl;
        } else if (path.startsWith("/api/artifacts")) {
            return artifactServiceUrl;
        } else if (path.startsWith("/api/projects")) {
            // Check if it's an analysis endpoint under project
            if (path.contains("/analyses")) {
                return analysisServiceUrl;
            }
            // Check if it's an artifact endpoint under project
            if (path.contains("/artifacts")) {
                return projectServiceUrl; // Project service handles artifact upload routing
            }
            return projectServiceUrl;
        }
        return null;
    }
}