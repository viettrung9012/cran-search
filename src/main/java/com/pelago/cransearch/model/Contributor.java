package com.pelago.cransearch.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

@Data
@Builder
@ToString
public class Contributor {
    @NonNull
    private String name;
    private String email;
}
