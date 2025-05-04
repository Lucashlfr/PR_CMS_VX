package com.messdiener.cms.v3.app.services.person;

import com.messdiener.cms.v3.app.entities.person.data.EmergencyContact;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmergencyContactService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmergencyContactService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_person_emergency_contact (contact_id VARCHAR(255), person_id VARCHAR(255), type VARCHAR(255), firstname VARCHAR(255), lastname VARCHAR(255), phone VARCHAR(255), mail VARCHAR(255), active BOOLEAN)";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("EmergencyContactService initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize EmergencyContactService", e);
        }
    }

    public void saveEmergencyContact(UUID personId, EmergencyContact emergencyContact) throws SQLException {
        String sql = "INSERT INTO module_person_emergency_contact (contact_id, person_id, type, firstname, lastname, phone, mail, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, emergencyContact.getContactId().toString());
            preparedStatement.setString(2, personId.toString());
            preparedStatement.setString(3, emergencyContact.getType());
            preparedStatement.setString(4, emergencyContact.getFirstName());
            preparedStatement.setString(5, emergencyContact.getLastName());
            preparedStatement.setString(6, emergencyContact.getPhoneNumber());
            preparedStatement.setString(7, emergencyContact.getMail());
            preparedStatement.setBoolean(8, emergencyContact.isActive());
            preparedStatement.executeUpdate();
            LOGGER.info("Emergency contact '{}' saved for person '{}'.", emergencyContact.getContactId(), personId);
        }
    }

    public List<EmergencyContact> getEmergencyContactsByPerson(UUID personId) throws SQLException {
        List<EmergencyContact> contacts = new ArrayList<>();
        String sql = "SELECT * FROM module_person_emergency_contact WHERE person_id = ? AND active ORDER BY lastname, firstname";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, personId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    UUID contactId = UUID.fromString(resultSet.getString("contact_id"));
                    String type = resultSet.getString("type");
                    String firstName = resultSet.getString("firstname");
                    String lastName = resultSet.getString("lastname");
                    String phoneNumber = resultSet.getString("phone");
                    String mail = resultSet.getString("mail");
                    boolean active = resultSet.getBoolean("active");

                    contacts.add(new EmergencyContact(contactId, type, firstName, lastName, phoneNumber, mail, active));
                }
            }
        }
        return contacts;
    }

    public void deleteEmergencyContact(UUID id) throws SQLException {
        String sql = "UPDATE module_person_emergency_contact SET active = 0 WHERE contact_id = ?";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, id.toString());
            preparedStatement.executeUpdate();
            LOGGER.info("Emergency contact '{}' marked as inactive.", id);
        }
    }

    public void deleteEmergencyContactsByUser(UUID owner) {
        databaseService.delete("module_person_emergency_contact", "person_id", owner.toString());
    }
}
