package com.messdiener.cms.v3.app.services.document;

import com.messdiener.cms.v3.app.entities.document.StorageFile;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.exception.StorageException;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STIconSetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_storage (documentId VARCHAR(255),  tag INT AUTO_INCREMENT PRIMARY KEY, owner VARCHAR(255), target VARCHAR(255), lastUpdate long, title TEXT, date long, meta double)")) {
            preparedStatement.executeUpdate();
            LOGGER.info("Configuration table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing configuration table", e);
            throw new RuntimeException(e);
        }
    }

    public String store(MultipartFile receiptFile, StorageFile storageFile) throws SQLException {
        if (receiptFile.isEmpty()) {
            throw new StorageException("Leere Datei kann nicht gespeichert werden.");
        }

        store(storageFile);

        String original = StringUtils.cleanPath(Objects.requireNonNull(receiptFile.getOriginalFilename()));
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx > 0) {
            ext = original.substring(idx);
        }

        String filename = storageFile.getId() + ext;
        try {
            Path root = Paths.get("./cms_vx/finance/");
            Files.createDirectories(root);
            Path target = root.resolve(filename);

            try (InputStream in = receiptFile.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            return filename;
        } catch (IOException e) {
            throw new StorageException("Fehler beim Speichern der Datei " + filename, e);
        }
    }

    public void store(StorageFile storageFile) throws SQLException {
        String sql = "INSERT INTO module_storage (documentId, tag, owner, target, lastUpdate, title, date, meta) VALUES (?,?,?,?,?,?,?,?)";
        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, storageFile.getId().toString());
            preparedStatement.setInt(2, storageFile.getTag());
            preparedStatement.setString(3, storageFile.getOwner().toString());
            preparedStatement.setString(4, storageFile.getTarget().toString());
            preparedStatement.setLong(5, storageFile.getLastUpdate().toLong());
            preparedStatement.setString(6, storageFile.getTitle());
            preparedStatement.setLong(7, storageFile.getDate().toLong());
            preparedStatement.setDouble(8, storageFile.getMeta());
            preparedStatement.executeUpdate();
        }
    }

    public File load(String filename) {
        Path root = Paths.get("./cms_vx/finance/").toAbsolutePath().normalize();
        String[] exts = {".png", ".pdf", ".jpg"};
        for (String ext : exts) {
            Path filePath = root.resolve(filename + ext).normalize();
            File file = filePath.toFile();
            if (file.exists() && file.isFile()) {
                return file;
            }
        }
        throw new StorageException("Datei nicht gefunden: " + filename);
    }

    private StorageFile getByResultSet(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("documentId"));
        int tag =  resultSet.getInt("tag");
        UUID owner =  UUID.fromString(resultSet.getString("owner"));
        UUID target =   UUID.fromString(resultSet.getString("target"));
        CMSDate lastUpdate = CMSDate.of(resultSet.getLong("lastUpdate"));
        String title =  resultSet.getString("title");
        CMSDate date =  CMSDate.of(resultSet.getLong("date"));
        double meta =   resultSet.getDouble("meta");
        return new StorageFile(id, tag, owner, target, lastUpdate, title, date, meta);
    }

    public List<StorageFile> loadFiles(UUID target) throws SQLException {
        List<StorageFile> storageFiles = new ArrayList<>();
        String sql = "SELECT * FROM module_storage WHERE target=? AND meta != 0";
        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, target.toString());
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while (resultSet.next()) {
                    storageFiles.add(getByResultSet(resultSet));
                }
            }
        }
        return storageFiles;
    }

    public double getSumm(UUID target) throws SQLException {
        String sql = "SELECT COALESCE(SUM(meta),0) AS sumAcc FROM module_storage WHERE target=?";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, target.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("sumAcc");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Fehler beim Ermitteln der Summe für transactionType ACCOUNT für Tenant {}", target, e);
        }
        return 0.0;
    }

}
