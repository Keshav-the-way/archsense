//package com.archsense.analysis.repository;
//
//import com.archsense.analysis.model.Project;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//
//@Repository
//public class ProjectRepository {
//
//    private final MongoTemplate projectMongoTemplate;
//
//    @Autowired
//    public ProjectRepository(@Qualifier("projectMongoTemplate") MongoTemplate projectMongoTemplate) {
//        this.projectMongoTemplate = projectMongoTemplate;
//    }
//
//    public Optional<Project> findByIdAndUserId(String id, String userId) {
//        Query query = new Query();
//        query.addCriteria(Criteria.where("_id").is(id).and("userId").is(userId));
//        Project project = projectMongoTemplate.findOne(query, Project.class);
//        return Optional.ofNullable(project);
//    }
//}