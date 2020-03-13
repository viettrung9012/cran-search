package com.pelago.cransearch.service;

import com.pelago.cransearch.model.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class MongoService {
    private MongoTemplate mongoTemplate;

    private Logger logger;

    @Autowired
    public MongoService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.logger = LoggerFactory.getLogger(MongoService.class);
    }

    public List<Package> findPackage(String query) {
        return mongoTemplate.find(query(where("name").regex(query, "i")), Package.class);
    }

    public void savePackage(Package pack) {
        try {
            mongoTemplate.save(pack);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
