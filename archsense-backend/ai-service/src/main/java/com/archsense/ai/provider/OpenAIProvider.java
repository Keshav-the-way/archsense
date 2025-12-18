package com.archsense.ai.provider;

import com.archsense.ai.model.ArtifactContent;
import com.archsense.common.dto.AnalysisReport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAIProvider implements AIProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAIProvider.class);
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Autowired(required = false)  // Make it optional since we might not have RestTemplate in ai-service standalone
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final int maxTokens;

    public OpenAIProvider(ObjectMapper objectMapper,
                          @Value("${ai.openai.api-key}") String apiKey,
                          @Value("${ai.openai.model}") String model,
                          @Value("${ai.openai.max-tokens}") int maxTokens) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.maxTokens = maxTokens;
    }

    @Override
    public AnalysisReport analyze(List<ArtifactContent> artifacts, String projectId, String analysisId) {
        log.info("OpenAI Provider: Analyzing {} artifacts for analysisId: {}", artifacts.size(), analysisId);

        if (restTemplate == null) {
            restTemplate = new RestTemplate();  // Fallback if not injected
        }

        String prompt = buildPrompt(artifacts);
        String aiResponse = callOpenAI(prompt);

        return parseAIResponse(aiResponse, analysisId, projectId);
    }

    // ... rest of the methods remain the same
    private String buildPrompt(List<ArtifactContent> artifacts) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert software architect. Analyze the following architecture artifacts and provide:\n\n");
        prompt.append("1. A summary with: overview, total components, total connections, and architecture pattern\n");
        prompt.append("2. A list of issues with: severity (HIGH/MEDIUM/LOW), category, description, and location\n");
        prompt.append("3. A list of recommendations with: priority (HIGH/MEDIUM/LOW), category, description, and rationale\n\n");
        prompt.append("Artifacts:\n");

        for (ArtifactContent artifact : artifacts) {
            prompt.append(String.format("- %s (%s): %d bytes\n",
                    artifact.getName(),
                    artifact.getType(),
                    artifact.getContentSize()));
        }

        prompt.append("\nProvide your analysis in JSON format with the structure: " +
                "{\"summary\": {...}, \"issues\": [...], \"recommendations\": [...]}");

        return prompt.toString();
    }

    private String callOpenAI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "You are an expert software architect."),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_API_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            return jsonResponse.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            log.error("Failed to call OpenAI API", e);
            throw new RuntimeException("OpenAI API call failed", e);
        }
    }

    private AnalysisReport parseAIResponse(String aiResponse, String analysisId, String projectId) {
        try {
            JsonNode json = objectMapper.readTree(aiResponse);

            AnalysisReport.Summary summary = parseSummary(json.path("summary"));
            List<AnalysisReport.Issue> issues = parseIssues(json.path("issues"));
            List<AnalysisReport.Recommendation> recommendations = parseRecommendations(json.path("recommendations"));

            return new AnalysisReport(analysisId, projectId, summary, issues, recommendations, Instant.now());

        } catch (Exception e) {
            log.error("Failed to parse AI response", e);
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }

    private AnalysisReport.Summary parseSummary(JsonNode summaryNode) {
        return new AnalysisReport.Summary(
                summaryNode.path("overview").asText(),
                summaryNode.path("totalComponents").asInt(),
                summaryNode.path("totalConnections").asInt(),
                summaryNode.path("architecturePattern").asText()
        );
    }

    private List<AnalysisReport.Issue> parseIssues(JsonNode issuesNode) {
        List<AnalysisReport.Issue> issues = new ArrayList<>();
        if (issuesNode.isArray()) {
            for (JsonNode issueNode : issuesNode) {
                issues.add(new AnalysisReport.Issue(
                        issueNode.path("severity").asText(),
                        issueNode.path("category").asText(),
                        issueNode.path("description").asText(),
                        issueNode.path("location").asText()
                ));
            }
        }
        return issues;
    }

    private List<AnalysisReport.Recommendation> parseRecommendations(JsonNode recommendationsNode) {
        List<AnalysisReport.Recommendation> recommendations = new ArrayList<>();
        if (recommendationsNode.isArray()) {
            for (JsonNode recNode : recommendationsNode) {
                recommendations.add(new AnalysisReport.Recommendation(
                        recNode.path("priority").asText(),
                        recNode.path("category").asText(),
                        recNode.path("description").asText(),
                        recNode.path("rationale").asText()
                ));
            }
        }
        return recommendations;
    }
}