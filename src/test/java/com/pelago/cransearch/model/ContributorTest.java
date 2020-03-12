package com.pelago.cransearch.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContributorTest {
    @Test
    public void testContributorNameNotNull() {
        assertThrows(NullPointerException.class, () -> Contributor.builder().email("test@example.com").build());
    }

    @Test
    public void testValidContributor() {
        final Contributor pack = Contributor.builder().name("test").email("test@example.com").build();
        assertEquals("test", pack.getName());
        assertEquals("test@example.com", pack.getEmail());
    }
}