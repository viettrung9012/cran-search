package com.pelago.cransearch.controller;

import com.pelago.cransearch.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SearchControllerTest {
    private SearchService service;
    private SearchController controller;

    @BeforeEach
    public void setUp() {
        service = mock(SearchService.class);
        controller = new SearchController(service);
    }

    @Test
    public void search() {
        final String query = "test package";
        controller.search(query);
        verify(service).searchPackages(eq(query));
    }
}