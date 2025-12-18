package com.archsense.executor.ai;

import com.archsense.executor.model.AnalysisReport;
import com.archsense.executor.model.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAnalysisEngine {

    private static final Logger log = LoggerFactory.getLogger(MockAnalysisEngine.class);

    public AnalysisReport analyze(List<Artifact> artifacts, Map<String, Object> context) {
        log.info("Starting MOCK analysis for {} artifacts", artifacts.size());

        AnalysisReport report = new AnalysisReport();
        report.setSummary("Mock analysis completed for " + artifacts.size() + " artifact(s). This is a simulated report.");

        List<AnalysisReport.Issue> issues = new ArrayList<>();
        issues.add(new AnalysisReport.Issue(
                "HIGH",
                "Security",
                "Missing authentication layer between API Gateway and microservices",
                "API Gateway"
        ));
        issues.add(new AnalysisReport.Issue(
                "MEDIUM",
                "Performance",
                "Database queries not optimized, missing indexes on frequently queried fields",
                "Database Layer"
        ));
        issues.add(new AnalysisReport.Issue(
                "LOW",
                "Best Practices",
                "Consider implementing circuit breaker pattern for external service calls",
                "Service Layer"
        ));
        report.setIssues(issues);

        List<AnalysisReport.Recommendation> recommendations = new ArrayList<>();
        recommendations.add(new AnalysisReport.Recommendation(
                "HIGH",
                "Implement Service-to-Service Authentication",
                "Add JWT-based authentication between internal services using service tokens",
                "Prevents unauthorized internal service access and improves security posture"
        ));
        recommendations.add(new AnalysisReport.Recommendation(
                "MEDIUM",
                "Add Database Indexes",
                "Create indexes on user_id, project_id, and created_at fields",
                "Improves query performance by 40-60% and reduces database load"
        ));
        recommendations.add(new AnalysisReport.Recommendation(
                "LOW",
                "Implement Health Check Endpoints",
                "Add /health endpoints for all microservices with detailed status",
                "Better monitoring, faster incident detection and response"
        ));
        report.setRecommendations(recommendations);

        log.info("Mock analysis completed with {} issues and {} recommendations",
                issues.size(), recommendations.size());

        return report;
    }
}