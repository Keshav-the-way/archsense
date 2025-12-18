package com.archsense.executor.ai;

import com.archsense.executor.model.AnalysisReport;
import com.archsense.executor.model.Artifact;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "anthropic")
public class ClaudeAnalysisEngine {

    private static final Logger log = LoggerFactory.getLogger(ClaudeAnalysisEngine.class);
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final int maxTokens;

    @Autowired(required = false)
    private com.archsense.executor.storage.StorageService artifactStorageService;

    public ClaudeAnalysisEngine(
            @Value("${ai.anthropic.api-key}") String apiKey,
            @Value("${ai.anthropic.model}") String model,
            @Value("${ai.anthropic.max-tokens}") int maxTokens,
            ObjectMapper objectMapper) {

        this.apiKey = apiKey;
        this.model = model;
        this.maxTokens = maxTokens;
        this.objectMapper = objectMapper;

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS) // Longer timeout for image analysis
                .build();

        log.info("Claude AI Engine initialized: model={}, maxTokens={}", model, maxTokens);
    }

    public AnalysisReport analyze(List<Artifact> artifacts, Map<String, Object> context) {
        log.info("Starting Claude AI analysis for {} artifacts", artifacts.size());

        try {
            String response = callClaudeAPI(artifacts, context);
            AnalysisReport report = parseResponse(response);

            log.info("Claude analysis completed: {} issues, {} recommendations",
                    report.getIssues().size(), report.getRecommendations().size());

            return report;

        } catch (Exception e) {
            log.error("Claude AI analysis failed", e);
            throw new RuntimeException("AI analysis failed: " + e.getMessage(), e);
        }
    }

    private String callClaudeAPI(List<Artifact> artifacts, Map<String, Object> context) throws IOException {
        String requestBody = buildRequestBodyWithImages(artifacts, context);

        Request request = new Request.Builder()
                .url(CLAUDE_API_URL)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        log.debug("Calling Claude API with {} artifacts...", artifacts.size());

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Claude API error: {} - {}", response.code(), errorBody);
                throw new IOException("Claude API request failed: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            log.debug("Claude API response received: {} bytes", responseBody.length());

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.path("content");

            if (content.isArray() && content.size() > 0) {
                return content.get(0).path("text").asText();
            }

            throw new IOException("Unexpected response format from Claude API");
        }
    }

    private String buildRequestBodyWithImages(List<Artifact> artifacts, Map<String, Object> context) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            var message = mapper.createObjectNode();
            message.put("model", model);
            message.put("max_tokens", maxTokens);

            var messages = mapper.createArrayNode();
            var userMessage = mapper.createObjectNode();
            userMessage.put("role", "user");

            // Build content array with text + images
            var contentArray = mapper.createArrayNode();

            // Add text prompt
            var textContent = mapper.createObjectNode();
            textContent.put("type", "text");
            textContent.put("text", buildPrompt(artifacts, context));
            contentArray.add(textContent);

            // Add images
            for (Artifact artifact : artifacts) {
                if (isImageType(artifact.getType())) {
                    log.info("Adding image to analysis: {}", artifact.getName());
                    String base64Image = getArtifactAsBase64(artifact);
                    if (base64Image != null) {
                        var imageContent = mapper.createObjectNode();
                        imageContent.put("type", "image");

                        var imageSource = mapper.createObjectNode();
                        imageSource.put("type", "base64");
                        imageSource.put("media_type", getMediaType(artifact.getType()));
                        imageSource.put("data", base64Image);

                        imageContent.set("source", imageSource);
                        contentArray.add(imageContent);
                    }
                }
            }

            userMessage.set("content", contentArray);
            messages.add(userMessage);

            message.set("messages", messages);

            return mapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build request body", e);
        }
    }

    private boolean isImageType(String type) {
        return type != null && (
                type.equalsIgnoreCase("IMAGE") ||
                        type.equalsIgnoreCase("PNG") ||
                        type.equalsIgnoreCase("JPEG") ||
                        type.equalsIgnoreCase("JPG") ||
                        type.equalsIgnoreCase("WEBP") ||
                        type.equalsIgnoreCase("GIF")
        );
    }

    private String getMediaType(String type) {
        if (type == null) return "image/png";

        return switch (type.toUpperCase()) {
            case "PNG" -> "image/png";
            case "JPEG", "JPG" -> "image/jpeg";
            case "WEBP" -> "image/webp";
            case "GIF" -> "image/gif";
            default -> "image/png";
        };
    }

    private String getArtifactAsBase64(Artifact artifact) {
        if (artifactStorageService == null) {
            log.warn("Storage service not available, cannot load artifact: {}", artifact.getName());
            return null;
        }

        try {
            log.debug("Loading artifact from storage: {}", artifact.getS3Key());
//            InputStream inputStream = artifactStorageService.retrieveReport(artifact.getS3Key());
//            byte[] imageBytes = inputStream.readAllBytes();
//            inputStream.close();
            String data = artifactStorageService.retrieveReport(artifact.getS3Key());
            InputStream inputStream =
                    new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
            byte[] imageBytes = inputStream.readAllBytes();
            inputStream.close();

            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            log.debug("Encoded artifact to base64: {} bytes -> {} chars", imageBytes.length, base64.length());

            return base64;
        } catch (Exception e) {
            log.error("Failed to load artifact: {}", artifact.getName(), e);
            return null;
        }
    }

    private String buildPrompt(List<Artifact> artifacts, Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert software architect analyzing system architecture artifacts.\n\n");

        prompt.append("CRITICAL TOKEN LIMIT: Your response must be under 9,500 tokens (leaving 500 token buffer).\n");
        prompt.append("Be comprehensive but concise. Prioritize quality over quantity.\n");
        prompt.append("If you approach the limit, prefer fewer detailed items over many shallow items.\n\n");

        // Extract context
        Integer version = (Integer) context.get("version");
        String previousReportJson = (String) context.get("previousReport");
        @SuppressWarnings("unchecked")
        Map<String, Object> constraints = (Map<String, Object>) context.get("constraints");

        prompt.append("=== ANALYSIS CONTEXT ===\n");
        prompt.append("Analysis Version: ").append(version != null ? version : 1).append("\n");

        if (constraints != null && !constraints.isEmpty()) {
            prompt.append("\n=== PROJECT CONSTRAINTS ===\n");
            if (constraints.get("expectedQps") != null) {
                prompt.append("Expected Load: ").append(constraints.get("expectedQps")).append(" QPS\n");
            }
            if (constraints.get("latencyTargetMs") != null) {
                prompt.append("Latency Target: ").append(constraints.get("latencyTargetMs")).append(" ms\n");
            }
            if (constraints.get("consistencyLevel") != null) {
                prompt.append("Consistency Requirement: ").append(constraints.get("consistencyLevel")).append("\n");
            }
            if (constraints.get("budgetSensitivity") != null) {
                prompt.append("Budget Sensitivity: ").append(constraints.get("budgetSensitivity")).append("\n");
            }
        }

        if (previousReportJson != null && !previousReportJson.isEmpty()) {
            prompt.append("\n=== PREVIOUS ANALYSIS (Version ").append(version != null ? version - 1 : "N-1").append(") ===\n");
            prompt.append("IMPORTANT: The previous analysis may have been performed on a DIFFERENT architecture artifact.\n");
            prompt.append("Your task is to compare the ISSUES and ARCHITECTURE QUALITY, not the specific artifact content.\n");
            prompt.append("Focus on:\n");
            prompt.append("- Which types of issues were present before vs now?\n");
            prompt.append("- Has the architecture improved, degraded, or stayed the same?\n");
            prompt.append("- Are there fewer/more HIGH severity issues?\n");
            prompt.append("- Is the overall system more/less resilient, scalable, secure?\n\n");
            prompt.append(previousReportJson).append("\n");
        }

        prompt.append("\n=== ARTIFACTS PROVIDED ===\n");
        for (Artifact artifact : artifacts) {
            prompt.append(String.format("- %s (%s, %d bytes)\n",
                    artifact.getName(), artifact.getType(), artifact.getSize()));
        }

        prompt.append("\n=== ANALYSIS INSTRUCTIONS ===\n");
        prompt.append("For architecture diagrams/images:\n");
        prompt.append("1. Identify all components, services, and systems\n");
        prompt.append("2. Analyze connections and data flows\n");
        prompt.append("3. Evaluate the architecture pattern (microservices, monolith, serverless, etc.)\n");
        prompt.append("4. Look for security vulnerabilities, single points of failure, performance bottlenecks\n\n");

        if (constraints != null && !constraints.isEmpty()) {
            prompt.append("5. EVALUATE ISSUES AGAINST CONSTRAINTS:\n");
            prompt.append("   - Assess severity based on project constraints\n");
            prompt.append("   - An issue is HIGH severity if it violates a constraint\n");
            prompt.append("   - Consider budget sensitivity when recommending solutions\n");
            prompt.append("   - Factor in expected load and latency targets\n\n");
        }

        if (previousReportJson != null && !previousReportJson.isEmpty()) {
            prompt.append("6. **MANDATORY** - COMPARE WITH PREVIOUS ANALYSIS VERSION:\n");
            prompt.append("   CRITICAL: The previous analysis above may be from a DIFFERENT architecture document.\n");
            prompt.append("   You MUST compare the ARCHITECTURE QUALITY, not document similarity.\n\n");

            prompt.append("   Step-by-step comparison process:\n");
            prompt.append("   a) List all HIGH/MEDIUM issues from the previous analysis\n");
            prompt.append("   b) Check if each previous issue still exists in the current architecture\n");
            prompt.append("   c) For RESOLVED issues: Issues that existed before but are now fixed\n");
            prompt.append("      Example: Previous had 'MD5 password hashing' → Current has 'bcrypt hashing' = RESOLVED\n");
            prompt.append("      Example: Previous had 'Single database instance' → Current has 'Multi-AZ cluster' = RESOLVED\n");
            prompt.append("   d) For NEW issues: Issues in current architecture that weren't in previous\n");
            prompt.append("      Example: Current architecture has 'No rate limiting' but previous did = NEW issue\n");
            prompt.append("   e) For REGRESSED issues: Issues that got WORSE\n");
            prompt.append("      Example: Previous had caching, current removed it = REGRESSION\n");
            prompt.append("   f) Determine overall trend:\n");
            prompt.append("      - IMPROVING: More issues resolved than new issues added, or severity decreased\n");
            prompt.append("      - STABLE: Similar number and severity of issues\n");
            prompt.append("      - DEGRADING: More new issues or increased severity\n\n");

            prompt.append("   EXAMPLES OF GOOD COMPARISONS:\n");
            prompt.append("   ✓ 'Previous had MD5 hashing (HIGH), current has bcrypt (secure)' → Resolved\n");
            prompt.append("   ✓ 'Previous had no database replication (HIGH), current has Multi-AZ' → Resolved\n");
            prompt.append("   ✓ 'Previous had synchronous processing (MEDIUM), current has queues' → Resolved\n");
            prompt.append("   ✓ 'Current has higher infrastructure costs (NEW)' → New Issue\n\n");

            prompt.append("   You MUST include evolutionAnalysis with meaningful comparisons.\n");
            prompt.append("   DO NOT say 'Unable to compare' - always attempt comparison based on issue types.\n\n");
        }

        prompt.append("7. ESTIMATE CLOUD COSTS:\n");
        prompt.append("   - For each component, estimate monthly AWS/cloud cost\n");
        prompt.append("   - Consider: compute (EC2/Lambda), database (RDS/DynamoDB), storage (S3), networking\n");
        prompt.append("   - Provide component-level breakdown\n");
        prompt.append("   - Suggest cost optimizations based on budget sensitivity\n\n");

        prompt.append("8. CONFIDENCE SCORING:\n");
        prompt.append("   - For each issue, include a confidence score (0.0 to 1.0)\n");
        prompt.append("   - Lower confidence if information is unclear or ambiguous\n");
        prompt.append("   - Flag issues where you need more information\n\n");

        prompt.append("=== REQUIRED JSON RESPONSE FORMAT ===\n");
        prompt.append("Return ONLY valid JSON (no markdown, no code blocks) in this exact structure:\n");
        prompt.append("{\n");
        prompt.append("  \"version\": ").append(version != null ? version : 1).append(",\n");
        prompt.append("  \"summary\": \"Detailed analysis including pattern, components, and connections\",\n");
        prompt.append("  \"architecturePattern\": \"Microservices|Monolithic|Serverless|Hybrid|etc.\",\n");
        prompt.append("  \"components\": [\"Component1\", \"Component2\"],\n");
        prompt.append("  \"connections\": [\"Connection1: A->B\", \"Connection2: B->C\"],\n");
        prompt.append("  \"issues\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"severity\": \"HIGH|MEDIUM|LOW\",\n");
        prompt.append("      \"category\": \"Security|Performance|Scalability|Reliability|Cost\",\n");
        prompt.append("      \"description\": \"Detailed issue description\",\n");
        prompt.append("      \"location\": \"Where found\",\n");
        prompt.append("      \"confidenceScore\": 0.85\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"priority\": \"HIGH|MEDIUM|LOW\",\n");
        prompt.append("      \"title\": \"Clear title\",\n");
        prompt.append("      \"description\": \"Detailed recommendation\",\n");
        prompt.append("      \"benefit\": \"Expected benefit\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");

// CRITICAL: Make evolutionAnalysis MANDATORY for version > 1
        if (version != null && version > 1) {
            prompt.append("  \"evolutionAnalysis\": {\n");

            if (previousReportJson != null && !previousReportJson.isEmpty()) {
                prompt.append("  \"evolutionAnalysis\": {\n");
                prompt.append("    \"resolvedIssues\": [  // Aim for 5-15 items for different architectures\n");
                prompt.append("      \"Specific HIGH/MEDIUM severity issue that was fixed with clear before/after\",\n");
                prompt.append("      \"Another resolved issue with severity context and specific improvement\"\n");
                prompt.append("    ],\n");
                prompt.append("    \"newIssues\": [  // Issues in current that weren't in previous\n");
                prompt.append("      \"New issue that appeared (if any)\"\n");
                prompt.append("    ],\n");
                prompt.append("    \"regressedIssues\": [  // Issues that got WORSE\n");
                prompt.append("      \"Issue that regressed (if any)\"\n");
                prompt.append("    ],\n");
                prompt.append("    \"overallTrend\": \"IMPROVING|STABLE|DEGRADING (based on net change)\"\n");
                prompt.append("  },\n");
            } else {
                // Fallback if previous report couldn't be loaded
                prompt.append("    \"resolvedIssues\": [\"Previous report unavailable - cannot determine resolved issues\"],\n");
                prompt.append("    \"newIssues\": [],\n");
                prompt.append("    \"regressedIssues\": [],\n");
                prompt.append("    \"overallTrend\": \"UNKNOWN\"\n");
            }

            prompt.append("  },\n");
        }

        prompt.append("  \"costEstimation\": {\n");
        prompt.append("    \"monthlyEstimateUsd\": 0.0,\n");
        prompt.append("    \"costTier\": \"LOW|MEDIUM|HIGH\",\n");
        prompt.append("    \"componentCosts\": [\n");
        prompt.append("      {\n");
        prompt.append("        \"componentName\": \"Component name\",\n");
        prompt.append("        \"serviceType\": \"compute|database|storage|network|other\",\n");
        prompt.append("        \"estimatedMonthlyCost\": 0.0\n");
        prompt.append("      }\n");
        prompt.append("    ],\n");
        prompt.append("    \"costOptimizations\": [\"Optimization suggestion\"]\n");
        prompt.append("  }\n");
        prompt.append("}");

        return prompt.toString();
    }

    private AnalysisReport parseResponse(String response) {
        try {
            String jsonStr = response.trim();

            // Remove markdown code blocks if present
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7);
            }
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();

            log.debug("Parsing Claude response...");
            AnalysisReport report = objectMapper.readValue(jsonStr, AnalysisReport.class);

            // VALIDATION: Ensure evolutionAnalysis exists and is meaningful for version > 1
            if (report.getVersion() != null && report.getVersion() > 1) {
                if (report.getEvolutionAnalysis() == null) {
                    log.error("Version {} but evolutionAnalysis is NULL - creating default", report.getVersion());
                    AnalysisReport.EvolutionAnalysis evolution = new AnalysisReport.EvolutionAnalysis();
                    evolution.setResolvedIssues(List.of("AI did not generate evolution analysis"));
                    evolution.setNewIssues(new ArrayList<>());
                    evolution.setRegressedIssues(new ArrayList<>());
                    evolution.setOverallTrend("UNKNOWN");
                    report.setEvolutionAnalysis(evolution);
                } else {
                    // Check if lists are empty - might indicate AI didn't do proper comparison
                    AnalysisReport.EvolutionAnalysis evo = report.getEvolutionAnalysis();
                    boolean allEmpty = (evo.getResolvedIssues() == null || evo.getResolvedIssues().isEmpty()) &&
                            (evo.getNewIssues() == null || evo.getNewIssues().isEmpty()) &&
                            (evo.getRegressedIssues() == null || evo.getRegressedIssues().isEmpty());

                    if (allEmpty) {
                        log.warn("Version {} evolutionAnalysis has all empty lists - AI may not have compared properly",
                                report.getVersion());
                        // Don't override - let it be empty to indicate comparison wasn't done
                    }
                }
            }

            return report;

        } catch (Exception e) {
            log.error("Failed to parse Claude response: {}",
                    response.substring(0, Math.min(500, response.length())), e);

            // Fallback report
            AnalysisReport report = new AnalysisReport();
            report.setSummary("Analysis completed but response format was unexpected. Please try again.");
            report.setArchitecturePattern("Unknown");
            report.setComponents(new ArrayList<>());
            report.setConnections(new ArrayList<>());
            report.setIssues(new ArrayList<>());
            report.setRecommendations(new ArrayList<>());

            return report;
        }
    }

//    private String attemptJsonRepair(String truncatedJson) {
//        log.warn("Attempting to repair truncated JSON response");
//
//        // Try to close any open strings and objects
//        StringBuilder repaired = new StringBuilder(truncatedJson);
//
//        // Count open braces/brackets
//        int openBraces = 0;
//        int openBrackets = 0;
//        boolean inString = false;
//
//        for (int i = 0; i < truncatedJson.length(); i++) {
//            char c = truncatedJson.charAt(i);
//
//            if (c == '"' && (i == 0 || truncatedJson.charAt(i-1) != '\\')) {
//                inString = !inString;
//            }
//
//            if (!inString) {
//                if (c == '{') openBraces++;
//                if (c == '}') openBraces--;
//                if (c == '[') openBrackets++;
//                if (c == ']') openBrackets--;
//            }
//        }
//
//        // If we're in a string, close it
//        if (inString) {
//            repaired.append("\"");
//        }
//
//        // Close any open arrays
//        while (openBrackets > 0) {
//            repaired.append("]");
//            openBrackets--;
//        }
//
//        // Close any open objects
//        while (openBraces > 0) {
//            repaired.append("}");
//            openBraces--;
//        }
//
//        String repairedJson = repaired.toString();
//        log.info("JSON repair attempted. Original length: {}, Repaired length: {}",
//                truncatedJson.length(), repairedJson.length());
//
//        return repairedJson;
//    }
}