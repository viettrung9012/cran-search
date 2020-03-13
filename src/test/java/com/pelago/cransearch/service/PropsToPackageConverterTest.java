package com.pelago.cransearch.service;

import com.pelago.cransearch.model.Contributor;
import com.pelago.cransearch.model.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropsToPackageConverterTest {

    private Properties props;
    private PropsToPackageConverter converter;

    @BeforeEach
    public void setUp() {
        props = new Properties();
        converter = new PropsToPackageConverter();
    }

    @Test
    public void getPackageFromProps() {
        final String packageName = "test package";
        final String version = "1.0";
        final String description = "test description";
        final String title = "test title";
        props.put("Description", description);
        props.put("Title", title);
        props.put("Date/Publication", "2020-08-03 12:00:01 UTC");
        props.put("Author", "Alice <alice@example.com> [cre], Bob");
        props.put("Maintainer", "Bob <bob@bob.bank>, Candice");

        final Package pack = converter.getPackageFromProps(packageName, version, props);
        assertEquals(packageName, pack.getName());
        assertEquals(version, pack.getVersion());
        assertEquals(description, pack.getDescription());
        assertEquals(title, pack.getTitle());

        final LocalDateTime expectedDatePublication = LocalDateTime.of(2020, 8, 3, 12, 0, 1);
        assertEquals(expectedDatePublication, pack.getDatePublication());

        final List<Contributor> authors = pack.getAuthors();
        assertEquals(2, authors.size());
        assertEquals("Alice", authors.get(0).getName());
        assertEquals("alice@example.com", authors.get(0).getEmail());
        assertEquals("Bob", authors.get(1).getName());
        assertEquals("bob@bob.bank", authors.get(1).getEmail());

        final List<Contributor> maintainers = pack.getMaintainers();
        assertEquals(2, maintainers.size());
        assertEquals("Bob", maintainers.get(0).getName());
        assertEquals("bob@bob.bank", maintainers.get(0).getEmail());
        assertEquals("Candice", maintainers.get(1).getName());
        assertNull(maintainers.get(1).getEmail());
    }

    @Test
    public void convertContributorRawDataToList() {
        final Map<String, Contributor> map = new HashMap<>();
        final Contributor alice = Contributor.builder().name("Alice").email("alice@example.com").build();
        final Contributor bob = Contributor.builder().name("Bob").build();
        map.put("Alice", alice);
        map.put("Bob", bob);
        final List<String> rawData = Arrays.asList("Alice   <alice@example.com>", "Bob  ");
        final List<Contributor> contributors = converter.convertContributorRawDataToList(rawData, map);
        assertEquals(Arrays.asList(alice, bob), contributors);
    }

    @Test
    public void getContributorMap() {
        final List<String> maintainerRawData = Arrays.asList("Alice  <alice@example.com>", "Bob", "Candice <tester>");
        final List<String> authorRawData = Arrays.asList("Bob       <bob@bob.bank>", "Daniel");
        final Map<String, Contributor> map = converter.getContributorMap(maintainerRawData, authorRawData);
        assertEquals(4, map.size());
        assertTrue(map.containsKey("Alice"));
        assertEquals("alice@example.com", map.get("Alice").getEmail());
        assertEquals("bob@bob.bank", map.get("Bob").getEmail());
        assertTrue(map.containsKey("Candice <tester>"));
        assertNull(map.get("Daniel").getEmail());
    }

    @Test
    public void getContributorRawData() {
        props.put("Author", "Alice, Bob");
        final List<String> contributorRawData = converter.getContributorRawData(props, "Author");
        final List<String> expected = Arrays.asList("Alice", "Bob");
        assertEquals(expected, contributorRawData);
    }

    @Test
    public void getContributorRawDataWithNoiseRemoved() {
        props.put("Author", "Alice [cre], Bob [uat]");
        final List<String> contributorRawData = converter.getContributorRawData(props, "Author");
        final List<String> expected = Arrays.asList("Alice", "Bob");
        assertEquals(expected, contributorRawData);
    }

    @Test
    public void getContributorRawDataKeepEmailAddress() {
        props.put("Author", "Alice <alice@example.com> [cre], Bob [uat]");
        final List<String> contributorRawData = converter.getContributorRawData(props, "Author");
        final List<String> expected = Arrays.asList("Alice <alice@example.com>", "Bob");
        assertEquals(expected, contributorRawData);
    }

    @Test
    public void getDatePublicationShouldReturnDate() {
        props.put("Date/Publication", "2020-03-08 12:00:01");
        final LocalDateTime expected = LocalDateTime.of(2020, 3, 8, 12, 0, 1);
        assertEquals(expected, converter.getDatePublication(props));
    }

    @Test
    public void getDatePublicationShouldReturnDateWhenPropsHasZone() {
        props.put("Date/Publication", "2020-03-08 12:00:01 UTC");
        final LocalDateTime expected = LocalDateTime.of(2020, 3, 8, 12, 0, 1);
        assertEquals(expected, converter.getDatePublication(props));
    }
}