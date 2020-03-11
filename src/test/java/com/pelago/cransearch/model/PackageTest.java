package com.pelago.cransearch.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PackageTest {
    @Test
    public void testPackageNameNotNull() {
        assertThrows(NullPointerException.class, () -> Package.builder().build());
    }
}