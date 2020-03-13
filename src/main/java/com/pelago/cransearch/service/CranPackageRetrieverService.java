package com.pelago.cransearch.service;

import com.pelago.cransearch.model.Package;
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

@Service
public class CranPackageRetrieverService {
    private static final String DESCRIPTION_FILE_NAME = "DESCRIPTION";
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
    private PackageExtractor packageExtractor;
    private PropsToPackageConverter propsToPackageConverter;

    private Logger logger;

    @Autowired
    public CranPackageRetrieverService(RestTemplate restTemplate, MongoService mongoService,
                                       PackageExtractor packageExtractor, PropsToPackageConverter propsToPackageConverter) {
        this.restTemplate = restTemplate;
        this.mongoService = mongoService;
        this.packageExtractor = packageExtractor;
        this.propsToPackageConverter = propsToPackageConverter;
        this.logger = LoggerFactory.getLogger(CranPackageRetrieverService.class);
    }

    @Scheduled(initialDelayString = "${package-retriever.initial-delay}", fixedDelayString = "${package-retriever.fixed-rate}")
    public void retrieveCranPackages() {
        logger.info("Retrieving packages...");
        final ResponseEntity<String> responseEntity = restTemplate.getForEntity(PACKAGE_SUMMARY_URL, String.class);
        final String body = responseEntity.getBody();
        final String[] packageSummaries = StringUtils.delimitedListToStringArray(body, EMPTY_SINGLE_LINE);
        Arrays.stream(packageSummaries).limit(PACKAGES_LIMIT).parallel().forEach(this::processPackageSummary);
        logger.info("Packages retrieved!");
    }

    private void processPackageSummary(String pack) {
        final String[] packSummary = StringUtils.delimitedListToStringArray(pack, NEW_LINE);
        final Properties props = StringUtils.splitArrayElementsIntoProperties(packSummary, PROPS_DELIMITER);

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
        final File file = restTemplate.execute(uri, HttpMethod.GET, null, writeResponseToFile(filePrefix));

        final File targetDir = Files.createTempDirectory(filePrefix).toFile();
        packageExtractor.extract(Objects.requireNonNull(file), targetDir);

        final Package pack = readDescriptionAndBuildPackage(targetDir, packageName, version);
        FileSystemUtils.deleteRecursively(targetDir);
        FileSystemUtils.deleteRecursively(file);
        return pack;
    }

    private ResponseExtractor<File> writeResponseToFile(String filePrefix) {
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
        return propsToPackageConverter.getPackageFromProps(packageName, version, props);
    }

    private Properties getPropertiesFromDescriptionFile(File targetDir, String packageName) throws IOException {
        final Path path = Paths.get(targetDir.getPath(), packageName, DESCRIPTION_FILE_NAME);
        final String[] description = Files.readAllLines(path).toArray(new String[0]);

        final Properties props = StringUtils.splitArrayElementsIntoProperties(description, PROPS_DELIMITER);
        Objects.requireNonNull(props);
        return props;
    }
}
