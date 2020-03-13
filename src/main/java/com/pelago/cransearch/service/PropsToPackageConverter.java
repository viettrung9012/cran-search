package com.pelago.cransearch.service;

import com.pelago.cransearch.model.Contributor;
import com.pelago.cransearch.model.Package;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class PropsToPackageConverter {
    private static final String EMAIL_REGEX = "<(.*?)>";
    private static final String NOISE_IN_NAME_REGEX = "\\[(.*?)]";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[ z]");
    private static final String EMPTY_STR = "";

    public Package getPackageFromProps(String packageName, String version, Properties props) {
        final List<String> maintainersRawData = getContributorRawData(props, PackageInfoFields.MAINTAINER.getName());
        final List<String> authorsRawData = getContributorRawData(props, PackageInfoFields.AUTHOR.getName());
        final Map<String, Contributor> contributors = getContributorMap(maintainersRawData, authorsRawData);

        return Package.builder()
                .name(packageName)
                .version(version)
                .description(props.getProperty(PackageInfoFields.DESCRIPTION.getName()))
                .title(props.getProperty(PackageInfoFields.TITLE.getName()))
                .datePublication(getDatePublication(props))
                .maintainers(convertContributorRawDataToList(maintainersRawData, contributors))
                .authors(convertContributorRawDataToList(authorsRawData, contributors))
                .build();
    }

    protected List<Contributor> convertContributorRawDataToList(List<String> rawData, Map<String, Contributor> contributors) {
        return rawData.stream()
                .map(name -> name.replaceAll(EMAIL_REGEX, EMPTY_STR).trim())
                .map(contributors::get)
                .collect(Collectors.toList());
    }

    protected Map<String, Contributor> getContributorMap(List<String> maintainersRawData, List<String> authorsRawData) {
        final Map<String, Contributor> contributors = new HashMap<>();

        Stream.concat(maintainersRawData.stream(), authorsRawData.stream()).flatMap(Stream::of).forEach(str -> {
            final Pattern emailPattern = Pattern.compile(EMAIL_REGEX);
            final Matcher matcher = emailPattern.matcher(str);
            if (matcher.find() && EmailValidator.getInstance().isValid(matcher.group(1))) {
                final String email = matcher.group(1); // matcher.group(0) will include <>, matcher.group(1) returns contained string only
                final String name = str.replaceAll(EMAIL_REGEX, EMPTY_STR).trim();
                final Contributor contributor = Contributor.builder().name(name).email(email).build();
                contributors.put(name, contributor);
            } else if (!contributors.containsKey(str)) {
                final Contributor contributor = Contributor.builder().name(str).build();
                contributors.put(str, contributor);
            }
        });
        return contributors;
    }

    protected List<String> getContributorRawData(Properties props, String contributorType) {
        final String contributorRawDataWithoutNoise = props.getProperty(contributorType).replaceAll(NOISE_IN_NAME_REGEX, EMPTY_STR);
        final String[] contributorsArr = StringUtils.commaDelimitedListToStringArray(contributorRawDataWithoutNoise);
        return Arrays.stream(contributorsArr).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    protected LocalDateTime getDatePublication(Properties props) {
        final String datePublicationString = props.getProperty(PackageInfoFields.DATE_PUBLICATION.getName());
        return LocalDateTime.parse(datePublicationString, DATE_TIME_FORMATTER);
    }
}
