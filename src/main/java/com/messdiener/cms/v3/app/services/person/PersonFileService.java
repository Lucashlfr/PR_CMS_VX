package com.messdiener.cms.v3.app.services.person;

import com.messdiener.cms.v3.utils.other.Pair;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonFileService.class);
    private static final String BASE_PATH = "." + File.separator + "cms_vx" + File.separator + "person";

    @PostConstruct
    public void init() {
        LOGGER.info("PersonFileService initialized.");
    }

    public Set<Pair<String, File>> listFilesUsingJavaIO(UUID personId) {
        Path dirPath = Path.of(BASE_PATH, personId.toString());

        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            return Collections.emptySet();
        }

        File[] files = dirPath.toFile().listFiles();
        if (files == null) {
            LOGGER.warn("Directory listing returned null for '{}'.", dirPath);
            return Collections.emptySet();
        }

        return Arrays.stream(files)
                .filter(file -> file.isFile())
                .map(file -> new Pair<>(file.getName(), file))
                .collect(Collectors.toSet());
    }

    public Set<Pair<String, File>> listFilesUsingJavaIO(UUID personId, int limit) {
        return listFilesUsingJavaIO(personId).stream()
                .limit(limit)
                .collect(Collectors.toSet());
    }

    public Optional<File> getFile(UUID personId, String filename) {
        Path dirPath = Path.of(BASE_PATH, personId.toString());

        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            LOGGER.info("Directory for user '{}' does not exist when trying to get file '{}'.", personId, filename);
            return Optional.empty();
        }

        File[] files = dirPath.toFile().listFiles();
        if (files == null) {
            LOGGER.warn("Directory listing returned null while trying to get file '{}'.", filename);
            return Optional.empty();
        }

        return Arrays.stream(files)
                .filter(file -> file.getName().equals(filename))
                .findFirst();
    }
}
