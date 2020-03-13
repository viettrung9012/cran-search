package com.pelago.cransearch.service;

import com.pelago.cransearch.model.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MongoServiceTest {
    private MongoTemplate template;
    private MongoService service;

    @BeforeEach
    public void setUp() {
        template = mock(MongoTemplate.class);
        service = new MongoService(template);
    }

    @Test
    public void findPackage() {
        when(template.find(any(Query.class), eq(Package.class))).thenReturn(Collections.emptyList());
        final ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        final String query = "test package";
        service.findPackage(query);
        verify(template, times(1)).find(queryCaptor.capture(), eq(Package.class));
        final Query capturedQuery = queryCaptor.getValue();
        assertNotNull(capturedQuery);
        assertTrue(capturedQuery.getQueryObject().containsKey("name"));
        assertEquals(query, capturedQuery.getQueryObject().get("name").toString());
    }
}