package com.archsense.executor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class AnalysisReport {

    private String analysisId;
    private String summary;

    @JsonProperty("architecturePattern")
    private String architecturePattern;

    private List<String> components;
    private List<String> connections;
    private List<Issue> issues;
    private List<Recommendation> recommendations;
    private Instant generatedAt;
    private Integer version;
    private EvolutionAnalysis evolutionAnalysis;
    private CostEstimation costEstimation;

    public static class EvolutionAnalysis {
        private List<String> resolvedIssues;
        private List<String> newIssues;
        private List<String> regressedIssues;
        private String overallTrend;  // "IMPROVING", "STABLE", "DEGRADING"

        public EvolutionAnalysis() {}

        public List<String> getResolvedIssues() { return resolvedIssues; }
        public void setResolvedIssues(List<String> resolvedIssues) { this.resolvedIssues = resolvedIssues; }

        public List<String> getNewIssues() { return newIssues; }
        public void setNewIssues(List<String> newIssues) { this.newIssues = newIssues; }

        public List<String> getRegressedIssues() { return regressedIssues; }
        public void setRegressedIssues(List<String> regressedIssues) { this.regressedIssues = regressedIssues; }

        public String getOverallTrend() { return overallTrend; }
        public void setOverallTrend(String overallTrend) { this.overallTrend = overallTrend; }
    }

    public static class CostEstimation {
        private Double monthlyEstimateUsd;
        private List<ComponentCost> componentCosts;
        private String costTier;  // "LOW", "MEDIUM", "HIGH"
        private List<String> costOptimizations;

        public CostEstimation() {}

        public Double getMonthlyEstimateUsd() { return monthlyEstimateUsd; }
        public void setMonthlyEstimateUsd(Double monthlyEstimateUsd) { this.monthlyEstimateUsd = monthlyEstimateUsd; }

        public List<ComponentCost> getComponentCosts() { return componentCosts; }
        public void setComponentCosts(List<ComponentCost> componentCosts) { this.componentCosts = componentCosts; }

        public String getCostTier() { return costTier; }
        public void setCostTier(String costTier) { this.costTier = costTier; }

        public List<String> getCostOptimizations() { return costOptimizations; }
        public void setCostOptimizations(List<String> costOptimizations) { this.costOptimizations = costOptimizations; }
    }

    public static class ComponentCost {
        private String componentName;
        private String serviceType;  // "compute", "database", "storage", "network", "other"
        private Double estimatedMonthlyCost;

        public ComponentCost() {}

        public ComponentCost(String componentName, String serviceType, Double estimatedMonthlyCost) {
            this.componentName = componentName;
            this.serviceType = serviceType;
            this.estimatedMonthlyCost = estimatedMonthlyCost;
        }

        // Getters and Setters
        public String getComponentName() { return componentName; }
        public void setComponentName(String componentName) { this.componentName = componentName; }

        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }

        public Double getEstimatedMonthlyCost() { return estimatedMonthlyCost; }
        public void setEstimatedMonthlyCost(Double estimatedMonthlyCost) { this.estimatedMonthlyCost = estimatedMonthlyCost; }
    }


    public AnalysisReport() {
        this.generatedAt = Instant.now();
    }

    // Getters and Setters

    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getArchitecturePattern() {
        return architecturePattern;
    }

    public void setArchitecturePattern(String architecturePattern) {
        this.architecturePattern = architecturePattern;
    }

    public List<String> getComponents() {
        return components;
    }

    public void setComponents(List<String> components) {
        this.components = components;
    }

    public List<String> getConnections() {
        return connections;
    }

    public void setConnections(List<String> connections) {
        this.connections = connections;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public static class Issue {
        private String severity;
        private String category;
        private String description;
        private String location;
        private Double confidenceScore;

        public Issue() {}

        public Issue(String severity, String category, String description, String location) {
            this.severity = severity;
            this.category = category;
            this.description = description;
            this.location = location;
        }

        public Double getConfidenceScore() { return confidenceScore; }

        public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    public static class Recommendation {
        private String priority;
        private String title;
        private String description;
        private String benefit;

        public Recommendation() {}

        public Recommendation(String priority, String title, String description, String benefit) {
            this.priority = priority;
            this.title = title;
            this.description = description;
            this.benefit = benefit;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getBenefit() {
            return benefit;
        }

        public void setBenefit(String benefit) {
            this.benefit = benefit;
        }
    }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public EvolutionAnalysis getEvolutionAnalysis() { return evolutionAnalysis; }
    public void setEvolutionAnalysis(EvolutionAnalysis evolutionAnalysis) { this.evolutionAnalysis = evolutionAnalysis; }

    public CostEstimation getCostEstimation() { return costEstimation; }
    public void setCostEstimation(CostEstimation costEstimation) { this.costEstimation = costEstimation; }
}