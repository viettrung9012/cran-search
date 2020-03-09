package com.pelago.cransearch.controller;

import com.pelago.cransearch.model.Package;
import com.pelago.cransearch.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {
    private SearchService service;

    @Autowired
    public SearchController(SearchService service) {
        this.service = service;
    }

    @RequestMapping(path = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Package> search(@RequestParam(value = "q") String query) {
        return service.searchPackages(query);
    }
}
