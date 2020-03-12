package com.pelago.cransearch.service;

public enum PackageInfoFields {
    PACKAGE("Package"),
    VERSION("Version"),
    MAINTAINER("Maintainer"),
    AUTHOR("Author"),
    DESCRIPTION("Description"),
    TITLE("Title"),
    DATE_PUBLICATION("Date/Publication");

    private String name;
    PackageInfoFields(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
