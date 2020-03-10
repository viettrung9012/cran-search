package com.pelago.cransearch.controller;

import com.pelago.cransearch.model.Package;
import com.pelago.cransearch.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(value = "CRAN Package Search API")
public class SearchController {
    private SearchService service;

    @Autowired
    public SearchController(SearchService service) {
        this.service = service;
    }

    @ApiOperation(value = "Search for packages by package name")
    @RequestMapping(path = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Package> search(
            @ApiParam(value = "Query string to search (case insensitive)", required = true, example = "abc")
            @RequestParam(value = "q") String query) {
        return service.searchPackages(query);
    }
}
