package com.pelago.cransearch.service;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class PackageExtractor {
    private Logger logger;

    public PackageExtractor() {
        this.logger = LoggerFactory.getLogger(PackageExtractor.class);
    }

    /**
     * Extract .tar.gz file to target directory using Apache common-compress
     *
     * @param zipFile .tar.gz file to be extracted
     * @param targetDir target directory to store the extracted files
     * @throws IOException can happen during extraction
     */
    public void extract(File zipFile, File targetDir) throws IOException {
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
