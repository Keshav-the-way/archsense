//package com.archsense.analysis.model;
//
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//@Document(collection = "projects")
//public class Project {
//    @Id
//    private String id;
//    private String userId;
//    private ProjectConstraints constraints;
//
//    public static class ProjectConstraints {
//        private Integer expectedQps;
//        private Integer latencyTargetMs;
//        private String consistencyLevel;
//        private String budgetSensitivity;
//
//        public ProjectConstraints() {}
//
//        // Getters and Setters
//        public Integer getExpectedQps() { return expectedQps; }
//        public void setExpectedQps(Integer expectedQps) { this.expectedQps = expectedQps; }
//
//        public Integer getLatencyTargetMs() { return latencyTargetMs; }
//        public void setLatencyTargetMs(Integer latencyTargetMs) { this.latencyTargetMs = latencyTargetMs; }
//
//        public String getConsistencyLevel() { return consistencyLevel; }
//        public void setConsistencyLevel(String consistencyLevel) { this.consistencyLevel = consistencyLevel; }
//
//        public String getBudgetSensitivity() { return budgetSensitivity; }
//        public void setBudgetSensitivity(String budgetSensitivity) { this.budgetSensitivity = budgetSensitivity; }
//    }
//
//    // Getters and Setters
//    public String getId() { return id; }
//    public void setId(String id) { this.id = id; }
//
//    public String getUserId() { return userId; }
//    public void setUserId(String userId) { this.userId = userId; }
//
//    public ProjectConstraints getConstraints() { return constraints; }
//    public void setConstraints(ProjectConstraints constraints) { this.constraints = constraints; }
//}