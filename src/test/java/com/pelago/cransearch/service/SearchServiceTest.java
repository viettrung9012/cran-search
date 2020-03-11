package com.pelago.cransearch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SearchServiceTest {
    private MongoService mongoService;
    private SearchService searchService;

    @BeforeEach
    public void setUp() {
        mongoService = mock(MongoService.class);
        searchService = new SearchService(mongoService);
    }

    @Test
    public void searchPackages() {
        final String query = "test package";
        searchService.searchPackages(query);
        verify(mongoService, times(1)).findPackage(eq(query));
    }
}