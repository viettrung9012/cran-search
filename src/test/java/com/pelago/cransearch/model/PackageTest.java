package com.pelago.cransearch.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PackageTest {
    @Test
    public void testPackageNameNotNull() {
        assertThrows(NullPointerException.class, () -> Package.builder().version("1.0").build());
    }

    @Test
    public void testVersionNotNull() {
        assertThrows(NullPointerException.class, () -> Package.builder().name("test package").build());
    }

    @Test
    public void testValidPackage() {
        final Package pack = Package.builder().name("test").version("1").build();
        assertEquals("test", pack.getName());
        assertEquals("1", pack.getVersion());
    }
}