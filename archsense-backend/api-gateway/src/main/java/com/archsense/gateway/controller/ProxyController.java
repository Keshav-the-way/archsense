package com.archsense.gateway.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Enumeration;

@RestController
@Tag(name = "ArchSense API", description = "All backend operations")
public class ProxyController {

    private static final Logger log = LoggerFactory.getLogger(ProxyController.class);

    private final RestTemplate restTemplate;
    private final String userServiceUrl;
    private final String projectServiceUrl;
    private final String artifactServiceUrl;
    private final String analysisServiceUrl;

    public ProxyController(RestTemplate restTemplate,
                           @Value("${services.user-service.base-url}") String userServiceUrl,
                           @Value("${services.project-service.base-url}") String projectServiceUrl,
                           @Value("${services.artifact-service.base-url}") String artifactServiceUrl,
                           @Value("${services.analysis-service.base-url}") String analysisServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
        this.projectServiceUrl = projectServiceUrl;
        this.artifactServiceUrl = artifactServiceUrl;
        this.analysisServiceUrl = analysisServiceUrl;
    }

    // ==================== USER ENDPOINTS ====================

    @Operation(summary = "Register user")
    @PostMapping("/api/users/register")
    public ResponseEntity<?> register(@RequestBody String body, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.POST, body);
    }

    @Operation(summary = "Login")
    @PostMapping("/api/users/login")
    public ResponseEntity<?> login(@RequestBody String body, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.POST, body);
    }

    @Operation(summary = "Get current user", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/api/users/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.GET, null);
    }

    @Operation(summary = "Forgot password")
    @PostMapping("/api/users/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody String body, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.POST, body);
    }

    @Operation(summary = "Reset password")
    @PostMapping("/api/users/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody String body, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.POST, body);
    }

    // ==================== PROJECT ENDPOINTS ====================

    @Operation(summary = "Create project", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/api/projects")
    public ResponseEntity<?> createProject(@RequestBody String body, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.POST, body);
    }

    @Operation(summary = "List projects", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/api/projects")
    public ResponseEntity<?> listProjects(HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.GET, null);
    }

    @Operation(summary = "Get project", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/api/projects/{id}")
    public ResponseEntity<?> getProject(@PathVariable String id, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.GET, null);
    }

    @Operation(summary = "Update project", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/api/projects/{id}")
    public ResponseEntity<?> updateProject(@PathVariable String id, @RequestBody String body, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.PUT, body);
    }

    @Operation(summary = "Delete project", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/api/projects/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable String id, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.DELETE, null);
    }

    // ==================== ARTIFACT ENDPOINTS ====================

    @Operation(summary = "Upload artifact", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/api/projects/{projectId}/artifacts")
    public ResponseEntity<?> uploadArtifact(
            @PathVariable String projectId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        log.info("Proxying file upload: projectId={}, file={}", projectId, file.getOriginalFilename());

        try {
            String userId = (String) request.getAttribute("X-User-Id");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("X-User-Id", userId);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Route to ARTIFACT SERVICE, not project service
            String url = artifactServiceUrl + "/api/projects/" + projectId + "/artifacts";
            log.info("Uploading to: {}", url);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            return ResponseEntity.status(response.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());

        } catch (HttpStatusCodeException e) {
            log.error("Artifact upload error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("File upload error", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\":\"Failed to upload file: " + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "List artifacts", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/api/projects/{projectId}/artifacts")
    public ResponseEntity<?> listArtifacts(@PathVariable String projectId, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.GET, null);
    }

    @Operation(summary = "Get artifact", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/api/artifacts/{id}")
    public ResponseEntity<?> getArtifact(@PathVariable String id, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.GET, null);
    }

    @Operation(summary = "Download artifact", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/api/artifacts/{id}/download")
    public ResponseEntity<?> downloadArtifact(@PathVariable String id, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.GET, null);
    }

    @Operation(summary = "Delete artifact", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/api/artifacts/{id}")
    public ResponseEntity<?> deleteArtifact(@PathVariable String id, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.DELETE, null);
    }

    // ==================== ANALYSIS ENDPOINTS ====================

    @Operation(summary = "Create analysis", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/api/projects/{projectId}/analyses")
    public ResponseEntity<?> createAnalysis(@PathVariable String projectId, @RequestBody String body, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.POST, body);
    }

    @Operation(summary = "List analyses", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/api/projects/{projectId}/analyses")
    public ResponseEntity<?> listAnalyses(@PathVariable String projectId, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.GET, null);
    }

    @Operation(summary = "Get analysis", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/api/analyses/{id}")
    public ResponseEntity<?> getAnalysis(@PathVariable String id, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.GET, null);
    }

    @Operation(summary = "Get analysis report", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/api/analyses/{id}/report")
    public ResponseEntity<?> getAnalysisReport(@PathVariable String id, HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.GET, null);
    }

    // ==================== INTERNAL PROXY LOGIC ====================

    @Hidden
    private ResponseEntity<?> proxyRequest(HttpServletRequest request, HttpMethod method, String body) {
        String path = request.getRequestURI();
        String targetUrl = determineTargetUrl(path);

        if (targetUrl == null) {
            log.error("No target URL found for path: {}", path);
            return ResponseEntity.notFound().build();
        }

        log.debug("Proxying {} {} to {}{}", method, path, targetUrl, path);

        try {
            HttpHeaders headers = buildHeaders(request);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    targetUrl + path,
                    method,
                    entity,
                    String.class
            );

            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpStatusCodeException e) {
            log.error("Proxy error: {} {} - Status: {}", method, path, e.getStatusCode());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Proxy error for {} {}", method, path, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\":\"Upstream service error: " + e.getMessage() + "\"}");
        }
    }

    private String determineTargetUrl(String path) {
        // Most specific patterns first
        if (path.matches("/api/projects/[^/]+/analyses.*")) {
            log.debug("Routing to analysis service: {}", path);
            return analysisServiceUrl;
        }

        if (path.matches("/api/projects/[^/]+/artifacts.*")) {
            log.debug("Routing to artifact service: {}", path);
            return artifactServiceUrl;
        }

        if (path.startsWith("/api/analyses")) {
            log.debug("Routing to analysis service: {}", path);
            return analysisServiceUrl;
        }

        if (path.startsWith("/api/artifacts")) {
            log.debug("Routing to artifact service: {}", path);
            return artifactServiceUrl;
        }

        if (path.startsWith("/api/projects")) {
            log.debug("Routing to project service: {}", path);
            return projectServiceUrl;
        }

        if (path.startsWith("/api/users")) {
            log.debug("Routing to user service: {}", path);
            return userServiceUrl;
        }

        log.warn("No route found for path: {}", path);
        return null;
    }

    private HttpHeaders buildHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.equalsIgnoreCase("Host") && !headerName.equalsIgnoreCase("Content-Length")) {
                headers.add(headerName, request.getHeader(headerName));
            }
        }

        String userId = (String) request.getAttribute("X-User-Id");
        if (userId != null) {
            headers.set("X-User-Id", userId);
        }

        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}