package com.pelago.cransearch.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import springfox.documentation.spi.DocumentationType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class SwaggerConfigTest {

    private SwaggerConfig config;
    private ViewControllerRegistry registry;
    private ViewControllerRegistration registration;

    @BeforeEach
    public void setUp() {
        this.config = new SwaggerConfig();
        this.registry = mock(ViewControllerRegistry.class);
        this.registration = mock(ViewControllerRegistration.class);
    }

    @Test
    public void apiDocket() {
        assertEquals(DocumentationType.SWAGGER_2, config.apiDocket().getDocumentationType());
    }

    @Test
    public void addViewControllers() {
        when(registry.addViewController(eq("/"))).thenReturn(registration);
        config.addViewControllers(registry);
        verify(registry).addViewController(eq("/"));
        verify(registration).setViewName(eq("redirect:/swagger-ui.html"));
    }
}