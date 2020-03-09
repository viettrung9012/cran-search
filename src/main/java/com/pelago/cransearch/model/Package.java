package com.pelago.cransearch.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class Package {
    private String packageName;
    private String version;
    private LocalDateTime datePublication;
    private String title;
    private String description;
    private List<Contributor> authors;
    private List<Contributor> maintainers;
}
