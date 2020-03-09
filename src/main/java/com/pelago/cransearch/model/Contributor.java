package com.pelago.cransearch.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class Contributor {
    private String name;
    private String email;
}
