package com.pelago.cransearch.service;

import com.pelago.cransearch.model.Contributor;
import com.pelago.cransearch.model.Package;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class SearchService {
    public List<Package> searchPackages(String query) {
        final List<Package> res = new ArrayList<>();
        final Contributor bob = Contributor.builder().name("Bob").email("bob@example.com").build();
        final Contributor alice = Contributor.builder().name("Alice").email("alice@example.com").build();
        final Package pack1 = Package.builder()
                .packageName("Test package")
                .version("1.0.0")
                .datePublication(LocalDateTime.now())
                .title("Test package")
                .description("Package for testing")
                .authors(Arrays.asList(bob, alice))
                .maintainers(Arrays.asList(bob, alice))
                .build();
        final Package pack2 = Package.builder()
                .packageName("Demo package")
                .version("Demo version")
                .datePublication(LocalDateTime.now())
                .title("Demo package")
                .description("Package for demonstration")
                .authors(Collections.singletonList(alice))
                .maintainers(Collections.emptyList())
                .build();
        res.add(pack1);
        res.add(pack2);
        return res;
    }
}
