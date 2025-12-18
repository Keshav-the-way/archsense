//package com.archsense.gateway.controller;
//
//import com.archsense.common.dto.request.*;
//import com.archsense.common.dto.response.*;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
///**
// * This controller exists ONLY for Swagger documentation.
// * Actual requests are proxied to backend services via ProxyFilter.
// */
//@RestController
//@RequestMapping("/api")
//public class SwaggerController {
//
//    // ==================== USER ENDPOINTS ====================
//
//    @Tag(name = "User Management", description = "User registration, login, and profile management")
//    @Operation(summary = "Register a new user", description = "Create a new user account")
//    @ApiResponse(responseCode = "201", description = "User registered successfully",
//            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
//    @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
//    @PostMapping("/users/register")
//    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "User Management")
//    @Operation(summary = "Login", description = "Authenticate and get JWT token")
//    @ApiResponse(responseCode = "200", description = "Login successful",
//            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
//    @ApiResponse(responseCode = "401", description = "Invalid credentials")
//    @PostMapping("/users/login")
//    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "User Management")
//    @Operation(summary = "Get current user profile", description = "Get authenticated user's profile",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @ApiResponse(responseCode = "200", description = "User profile retrieved",
//            content = @Content(schema = @Schema(implementation = UserResponse.class)))
//    @GetMapping("/users/me")
//    public ResponseEntity<UserResponse> getCurrentUser() {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "User Management")
//    @Operation(summary = "Update user profile", description = "Update authenticated user's profile",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @PutMapping("/users/me")
//    public ResponseEntity<UserResponse> updateProfile(@RequestBody UpdateProjectRequest request) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    // ==================== PROJECT ENDPOINTS ====================
//
//    @Tag(name = "Project Management", description = "CRUD operations for architecture projects")
//    @Operation(summary = "Create a new project", description = "Create a new architecture analysis project",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @ApiResponse(responseCode = "201", description = "Project created successfully",
//            content = @Content(schema = @Schema(implementation = ProjectResponse.class)))
//    @PostMapping("/projects")
//    public ResponseEntity<ProjectResponse> createProject(@RequestBody CreateProjectRequest request) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "Project Management")
//    @Operation(summary = "List all projects", description = "Get all projects for the authenticated user",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
//    @GetMapping("/projects")
//    public ResponseEntity<?> listProjects() {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "Project Management")
//    @Operation(summary = "Get project by ID", description = "Get detailed information about a specific project",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @GetMapping("/projects/{id}")
//    public ResponseEntity<ProjectResponse> getProject(@Parameter(description = "Project ID") @PathVariable String id) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "Project Management")
//    @Operation(summary = "Update project", description = "Update project name or description",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @PutMapping("/projects/{id}")
//    public ResponseEntity<ProjectResponse> updateProject(
//            @Parameter(description = "Project ID") @PathVariable String id,
//            @RequestBody UpdateProjectRequest request) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "Project Management")
//    @Operation(summary = "Delete project", description = "Soft delete a project (also deletes associated artifacts and analyses)",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @ApiResponse(responseCode = "204", description = "Project deleted successfully")
//    @DeleteMapping("/projects/{id}")
//    public ResponseEntity<Void> deleteProject(@Parameter(description = "Project ID") @PathVariable String id) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    // ==================== ARTIFACT ENDPOINTS ====================
//
//    @Tag(name = "Artifact Management")
//    @Operation(summary = "Upload artifact", description = "Upload an architecture diagram, document, or other file",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @ApiResponse(responseCode = "201", description = "Artifact uploaded successfully",
//            content = @Content(schema = @Schema(implementation = ArtifactResponse.class)))
//    @PostMapping("/api/projects/{projectId}/artifacts")
//    public ResponseEntity<ArtifactResponse> uploadArtifact(
//            @Parameter(description = "Project ID") @PathVariable String projectId,
//            @Parameter(description = "File to upload (max 50MB)")
//            @RequestPart("file") MultipartFile file) {  // Changed from @RequestParam to @RequestPart
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "Artifact Management")
//    @Operation(summary = "List artifacts", description = "Get all artifacts for a project",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @GetMapping("/projects/{projectId}/artifacts")
//    public ResponseEntity<?> listArtifacts(@Parameter(description = "Project ID") @PathVariable String projectId) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "Artifact Management")
//    @Operation(summary = "Get artifact details", description = "Get metadata about a specific artifact",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @GetMapping("/artifacts/{id}")
//    public ResponseEntity<ArtifactResponse> getArtifact(@Parameter(description = "Artifact ID") @PathVariable String id) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "Artifact Management")
//    @Operation(summary = "Download artifact", description = "Download the artifact file",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @GetMapping("/artifacts/{id}/download")
//    public ResponseEntity<?> downloadArtifact(@Parameter(description = "Artifact ID") @PathVariable String id) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "Artifact Management")
//    @Operation(summary = "Delete artifact", description = "Delete an artifact and its file",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @DeleteMapping("/artifacts/{id}")
//    public ResponseEntity<Void> deleteArtifact(@Parameter(description = "Artifact ID") @PathVariable String id) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    // ==================== ANALYSIS ENDPOINTS ====================
//
//    @Tag(name = "Analysis", description = "Trigger AI analysis and view reports")
//    @Operation(summary = "Trigger analysis", description = "Start AI-powered analysis of selected artifacts",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @ApiResponse(responseCode = "201", description = "Analysis started successfully",
//            content = @Content(schema = @Schema(implementation = AnalysisResponse.class)))
//    @PostMapping("/projects/{projectId}/analyses")
//    public ResponseEntity<AnalysisResponse> createAnalysis(
//            @Parameter(description = "Project ID") @PathVariable String projectId,
//            @RequestBody CreateAnalysisRequest request) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "Analysis")
//    @Operation(summary = "List analyses", description = "Get all analyses for a project",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @GetMapping("/projects/{projectId}/analyses")
//    public ResponseEntity<?> listAnalyses(@Parameter(description = "Project ID") @PathVariable String projectId) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "Analysis")
//    @Operation(summary = "Get analysis status", description = "Check the status of an analysis (PENDING, IN_PROGRESS, COMPLETED, FAILED)",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @GetMapping("/analyses/{id}")
//    public ResponseEntity<AnalysisResponse> getAnalysis(@Parameter(description = "Analysis ID") @PathVariable String id) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//
//    @Tag(name = "Analysis")
//    @Operation(summary = "Get analysis report", description = "Get the complete analysis report (only available when status is COMPLETED)",
//            security = @SecurityRequirement(name = "bearer-jwt"))
//    @ApiResponse(responseCode = "200", description = "Report retrieved successfully")
//    @ApiResponse(responseCode = "400", description = "Analysis not completed yet")
//    @GetMapping("/analyses/{id}/report")
//    public ResponseEntity<?> getAnalysisReport(@Parameter(description = "Analysis ID") @PathVariable String id) {
//        throw new UnsupportedOperationException("This is a documentation-only endpoint");
//    }
//}