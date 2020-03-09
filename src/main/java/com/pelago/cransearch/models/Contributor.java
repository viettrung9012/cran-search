package com.pelago.cransearch.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Contributor {
    private String name;
    private String email;
}
