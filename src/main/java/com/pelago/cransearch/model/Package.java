package com.pelago.cransearch.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@ToString
@Document(collection = "packages")
public class Package {
    @Indexed(unique = true)
    @NonNull
    private String name;
    @NonNull
    private String version;
    private LocalDateTime datePublication;
    private String title;
    private String description;
    private List<Contributor> authors;
    private List<Contributor> maintainers;
}
