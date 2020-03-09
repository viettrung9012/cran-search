package com.pelago.cransearch.service;

import com.pelago.cransearch.model.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {
    private MongoService mongoService;

    @Autowired
    public SearchService(MongoService mongoService) {
        this.mongoService = mongoService;
    }

    public List<Package> searchPackages(String query) {
        return mongoService.findPackage(query);
    }
}
