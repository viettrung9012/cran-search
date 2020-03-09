package com.pelago.cransearch.service;

import com.pelago.cransearch.model.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class MongoService {
    private MongoTemplate mongoTemplate;

    @Autowired
    public MongoService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Package> findPackage(String query) {
        return mongoTemplate.find(query(where("packageName").regex(query, "i")), Package.class);
    }
}
