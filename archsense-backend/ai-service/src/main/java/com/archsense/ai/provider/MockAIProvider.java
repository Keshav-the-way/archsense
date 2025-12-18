package com.archsense.ai.provider;

import com.archsense.ai.model.ArtifactContent;
import com.archsense.common.dto.AnalysisReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mock")
public class MockAIProvider implements AIProvider {

    private static final Logger log = LoggerFactory.getLogger(MockAIProvider.class);

    @Override
    public AnalysisReport analyze(List<ArtifactContent> artifacts, String projectId, String analysisId) {
        log.info("Mock AI Provider: Analyzing {} artifacts for analysisId: {}", artifacts.size(), analysisId);

        simulateProcessing();

        AnalysisReport.Summary summary = generateSummary(artifacts);
        List<AnalysisReport.Issue> issues = generateIssues(artifacts);
        List<AnalysisReport.Recommendation> recommendations = generateRecommendations(artifacts);

        return new AnalysisReport(
                analysisId,
                projectId,
                summary,
                issues,
                recommendations,
                Instant.now()
        );
    }

    private void simulateProcessing() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private AnalysisReport.Summary generateSummary(List<ArtifactContent> artifacts) {
        int totalComponents = 8 + artifacts.size();
        int totalConnections = 12 + (artifacts.size() * 2);

        String overview = String.format(
                "This architecture demonstrates a %s pattern with %d identified components. " +
                        "The system shows good separation of concerns with clear service boundaries. " +
                        "Key services include API Gateway, authentication, business logic, and data persistence layers.",
                determinePattern(totalComponents),
                totalComponents
        );

        return new AnalysisReport.Summary(
                overview,
                totalComponents,
                totalConnections,
                determinePattern(totalComponents)
        );
    }

    private String determinePattern(int componentCount) {
        if (componentCount > 10) {
            return "Microservices";
        } else if (componentCount > 5) {
            return "Service-Oriented Architecture";
        } else {
            return "Layered Architecture";
        }
    }

    private List<AnalysisReport.Issue> generateIssues(List<ArtifactContent> artifacts) {
        List<AnalysisReport.Issue> issues = new ArrayList<>();

        issues.add(new AnalysisReport.Issue(
                "HIGH",
                "Reliability",
                "Single point of failure detected in the authentication service. Consider implementing redundancy.",
                "Authentication Service"
        ));

        issues.add(new AnalysisReport.Issue(
                "HIGH",
                "Security",
                "API Gateway lacks rate limiting configuration. This may expose the system to DoS attacks.",
                "API Gateway"
        ));

        issues.add(new AnalysisReport.Issue(
                "MEDIUM",
                "Performance",
                "Database connection pooling configuration appears suboptimal. May cause performance degradation under load.",
                "Data Access Layer"
        ));

        issues.add(new AnalysisReport.Issue(
                "MEDIUM",
                "Scalability",
                "Synchronous communication between services may create bottlenecks. Consider event-driven patterns.",
                "Inter-Service Communication"
        ));

        issues.add(new AnalysisReport.Issue(
                "LOW",
                "Maintainability",
                "Inconsistent error handling patterns across services. Standardization would improve debugging.",
                "Error Handling"
        ));

        if (artifacts.size() > 5) {
            issues.add(new AnalysisReport.Issue(
                    "MEDIUM",
                    "Complexity",
                    "Large number of artifacts suggests high system complexity. Consider consolidation opportunities.",
                    "Overall Architecture"
            ));
        }

        return issues;
    }

    private List<AnalysisReport.Recommendation> generateRecommendations(List<ArtifactContent> artifacts) {
        List<AnalysisReport.Recommendation> recommendations = new ArrayList<>();

        recommendations.add(new AnalysisReport.Recommendation(
                "HIGH",
                "Reliability",
                "Implement circuit breaker pattern using libraries like Resilience4j for all external service calls",
                "Prevents cascading failures and improves system resilience. Circuit breakers detect failures " +
                        "and prevent the application from repeatedly trying to execute operations that are likely to fail."
        ));

        recommendations.add(new AnalysisReport.Recommendation(
                "HIGH",
                "Security",
                "Add comprehensive API rate limiting and implement OAuth 2.0 for service-to-service communication",
                "Protects against abuse and ensures secure inter-service communication. Rate limiting prevents " +
                        "resource exhaustion while OAuth 2.0 provides industry-standard authentication and authorization."
        ));

        recommendations.add(new AnalysisReport.Recommendation(
                "MEDIUM",
                "Observability",
                "Implement distributed tracing using OpenTelemetry and centralized logging with correlation IDs",
                "Enables end-to-end request tracking across services and simplifies debugging in distributed systems. " +
                        "Correlation IDs allow tracing a single user request through multiple services."
        ));

        recommendations.add(new AnalysisReport.Recommendation(
                "MEDIUM",
                "Scalability",
                "Introduce message queue (Kafka/RabbitMQ) for asynchronous communication between services",
                "Decouples services, improves scalability, and enables better handling of traffic spikes. " +
                        "Asynchronous messaging allows services to process requests at their own pace."
        ));

        recommendations.add(new AnalysisReport.Recommendation(
                "MEDIUM",
                "Performance",
                "Implement caching strategy using Redis for frequently accessed data and API responses",
                "Reduces database load and improves response times for read-heavy operations. Proper caching " +
                        "can reduce latency by 10-100x for cached content."
        ));

        recommendations.add(new AnalysisReport.Recommendation(
                "LOW",
                "Documentation",
                "Generate and maintain up-to-date API documentation using OpenAPI/Swagger specifications",
                "Improves developer experience and reduces onboarding time. Auto-generated documentation " +
                        "stays in sync with actual API implementation."
        ));

        recommendations.add(new AnalysisReport.Recommendation(
                "LOW",
                "Testing",
                "Implement contract testing between services using tools like Pact to prevent integration issues",
                "Ensures service compatibility during independent deployments. Contract tests verify that services " +
                        "can communicate correctly without requiring full integration test environments."
        ));

        return recommendations;
    }
}