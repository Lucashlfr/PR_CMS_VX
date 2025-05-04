package com.messdiener.cms.v3.app.services.finance;


import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiptService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService
                .getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_finance_receipt (id VARCHAR(36), tag INT AUTO_INCREMENT PRIMARY KEY, transactionId VARCHAR(36), description TEXT, date long, amount double)")) {
            preparedStatement.executeUpdate();
            LOGGER.info("module_finance_receipt table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing module_finance_receipt", e);
            throw new RuntimeException(e);
        }
        LOGGER.info("ReceiptCenter initialized.");
    }

}