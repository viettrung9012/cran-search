package com.pelago.cransearch.service;

import com.pelago.cransearch.model.Contributor;
import com.pelago.cransearch.model.Package;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CranPackageRetrieverService {
    private static final String EMAIL_REGEX = "<(.*?)>";
    private static final String NOISE_IN_NAME_REGEX = "\\[(.*?)]";
    private static final String DESCRIPTION_FILE_NAME = "DESCRIPTION";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[ z]");
    private static final String EMPTY_STR = "";
    private static final String NEW_LINE = "\n";
    private static final String EMPTY_SINGLE_LINE = "\n\n";
    private static final String PROPS_DELIMITER = ":";
    private static final String TAR_GZ_PREFIX_TEMPLATE = "%s_%s";
    private static final String TAR_GZ_SUFFIX = ".tar.gz";
    private static final String PACKAGE_SUMMARY_URL = "https://cran.r-project.org/src/contrib/PACKAGES";
    private static final String PACKAGE_DOWNLOAD_URL_TEMPLATE = "https://cran.r-project.org/src/contrib/%s.tar.gz";
    private static final int PACKAGES_LIMIT = 50;

    private RestTemplate restTemplate;
    private MongoService mongoService;

    private Logger logger;

    @Autowired
    public CranPackageRetrieverService(RestTemplate restTemplate, MongoService mongoService) {
        this.restTemplate = restTemplate;
        this.mongoService = mongoService;
        this.logger = LoggerFactory.getLogger(CranPackageRetrieverService.class);
    }

    @Scheduled(initialDelayString = "${package-retriever.initial-delay}", fixedDelayString = "${package-retriever.fixed-rate}")
    public void retrieveCranPackages() {
        logger.info("Retrieving packages...");
        final ResponseEntity<String> responseEntity = restTemplate.getForEntity(PACKAGE_SUMMARY_URL, String.class);
        final String body = responseEntity.getBody();
        final String[] packageSummaries = StringUtils.delimitedListToStringArray(body, EMPTY_SINGLE_LINE);
        Arrays.stream(packageSummaries).limit(PACKAGES_LIMIT).parallel().forEach(this::processPackageSummary);
        logger.info("Packages saved to DB!");
    }

    private void processPackageSummary(String pack) {
        final String[] packData = StringUtils.delimitedListToStringArray(pack, NEW_LINE);
        final Properties props = StringUtils.splitArrayElementsIntoProperties(packData, PROPS_DELIMITER);

        Objects.requireNonNull(props);
        final String packageName = props.getProperty(PackageInfoFields.PACKAGE.getName());
        final String version = props.getProperty(PackageInfoFields.VERSION.getName());
        if (StringUtils.isEmpty(packageName) || StringUtils.isEmpty(version)) {
            logger.error("Missing package name or version in " + pack);
        } else {
            downloadPackageAndSaveToDb(packageName, version);
        }
    }

    private void downloadPackageAndSaveToDb(String packageName, String version) {
        try {
            final Package pack = downloadPackageAndBuildModel(packageName, version);
            mongoService.savePackage(pack);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private Package downloadPackageAndBuildModel(String packageName, String version) throws IOException {
        final String filePrefix = String.format(TAR_GZ_PREFIX_TEMPLATE, packageName, version);
        final String uri = String.format(PACKAGE_DOWNLOAD_URL_TEMPLATE, filePrefix);
        final File file = restTemplate.execute(uri, HttpMethod.GET, null, getFileResponseExtractor(filePrefix));

        final File targetDir = Files.createTempDirectory(filePrefix).toFile();
        extract(Objects.requireNonNull(file), targetDir);

        final Package pack = readDescriptionAndBuildPackage(targetDir, packageName, version);
        FileSystemUtils.deleteRecursively(targetDir);
        FileSystemUtils.deleteRecursively(file);
        return pack;
    }

    private ResponseExtractor<File> getFileResponseExtractor(String filePrefix) {
        return clientHttpResponse -> {
            File ret = File.createTempFile(filePrefix, TAR_GZ_SUFFIX);
            OutputStream out = Files.newOutputStream(ret.toPath());
            StreamUtils.copy(clientHttpResponse.getBody(), out);
            out.close();
            return ret;
        };
    }

    private Package readDescriptionAndBuildPackage(File targetDir, String packageName, String version) throws IOException {
        final Properties props = getPropertiesFromDescriptionFile(targetDir, packageName);
        return getPackageFromProps(packageName, version, props);
    }

    private Package getPackageFromProps(String packageName, String version, Properties props) {
        final List<String> maintainersRawData = getContributorRawData(props, PackageInfoFields.MAINTAINER.getName());
        final List<String> authorsRawData = getContributorRawData(props, PackageInfoFields.AUTHOR.getName());
        final Map<String, Contributor> contributors = getContributorMap(maintainersRawData, authorsRawData);

        return Package.builder()
                .packageName(packageName)
                .version(version)
                .description(props.getProperty(PackageInfoFields.DESCRIPTION.getName()))
                .title(props.getProperty(PackageInfoFields.TITLE.getName()))
                .datePublication(getDatePublication(props))
                .maintainers(convertContributorRawDataToList(maintainersRawData, contributors))
                .authors(convertContributorRawDataToList(authorsRawData, contributors))
                .build();
    }

    private List<Contributor> convertContributorRawDataToList(List<String> maintainersRawData, Map<String, Contributor> contributors) {
        return maintainersRawData.stream()
                .map(name -> name.replaceAll(EMAIL_REGEX, EMPTY_STR).trim())
                .map(contributors::get)
                .collect(Collectors.toList());
    }

    private Map<String, Contributor> getContributorMap(List<String> maintainersRawData, List<String> authorsRawData) {
        final Map<String, Contributor> contributors = new HashMap<>();

        Stream.concat(maintainersRawData.stream(), authorsRawData.stream()).flatMap(Stream::of).forEach(str -> {
            final Pattern emailPattern = Pattern.compile(EMAIL_REGEX);
            final Matcher matcher = emailPattern.matcher(str);
            if (matcher.find() && EmailValidator.getInstance().isValid(matcher.group(1))) {
                final String email = matcher.group(1); // matcher.group(0) will include <>, matcher.group(1) returns contained string only
                final String name = str.replaceAll(EMAIL_REGEX, EMPTY_STR).trim();
                final Contributor contributor = Contributor.builder().name(name).email(email).build();
                contributors.put(name, contributor);
            } else {
                if (!contributors.containsKey(str)) {
                    final Contributor contributor = Contributor.builder().name(str).build();
                    contributors.put(str, contributor);
                }
            }
        });
        return contributors;
    }

    private List<String> getContributorRawData(Properties props, String contributorType) {
        final String contributorRawDataWithoutNoise = props.getProperty(contributorType).replaceAll(NOISE_IN_NAME_REGEX, EMPTY_STR);
        final String[] contributorsArr = StringUtils.commaDelimitedListToStringArray(contributorRawDataWithoutNoise);
        return Arrays.stream(contributorsArr).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private Properties getPropertiesFromDescriptionFile(File targetDir, String packageName) throws IOException {
        final Path path = Paths.get(targetDir.getPath(), packageName, DESCRIPTION_FILE_NAME);
        final String[] description = Files.readAllLines(path).toArray(new String[0]);

        final Properties props = StringUtils.splitArrayElementsIntoProperties(description, PROPS_DELIMITER);
        Objects.requireNonNull(props);
        return props;
    }

    private LocalDateTime getDatePublication(Properties props) {
        final String datePublicationString = props.getProperty(PackageInfoFields.DATE_PUBLICATION.getName());
        return LocalDateTime.parse(datePublicationString, DATE_TIME_FORMATTER);
    }

    private void extract(File zipFile, File targetDir) throws IOException {
        final InputStream fileInputStream = Files.newInputStream(zipFile.toPath());
        final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        final GzipCompressorInputStream gzipCompressorInputStream = new GzipCompressorInputStream(bufferedInputStream);
        final ArchiveInputStream in = new TarArchiveInputStream(gzipCompressorInputStream);

        ArchiveEntry entry;
        while ((entry = in.getNextEntry()) != null) {
            if (!in.canReadEntryData(entry)) {
                logger.error("Cannot read entry for entry=" + entry.getName());
                continue;
            }
            File f = Paths.get(targetDir.getPath(), entry.getName()).toFile();
            if (entry.isDirectory()) {
                if (!f.isDirectory() && !f.mkdirs()) {
                    throw new IOException("Failed to create directory " + f);
                }
            } else {
                File parent = f.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }
                try (OutputStream out = Files.newOutputStream(f.toPath())) {
                    IOUtils.copy(in, out);
                }
            }
        }
    }
}
